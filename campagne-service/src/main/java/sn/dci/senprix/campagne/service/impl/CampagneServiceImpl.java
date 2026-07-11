package sn.dci.senprix.campagne.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dci.senprix.campagne.client.ProduitServiceClient;
import sn.dci.senprix.campagne.client.UserServiceClient;
import sn.dci.senprix.campagne.client.UtilisateurVerificationDto;
import sn.dci.senprix.campagne.dto.CampagneRequest;
import sn.dci.senprix.campagne.dto.CampagneResponse;
import sn.dci.senprix.campagne.entity.Campagne;
import sn.dci.senprix.campagne.entity.CampagneAgent;
import sn.dci.senprix.campagne.entity.CampagneMarche;
import sn.dci.senprix.campagne.entity.CampagneProduit;
import sn.dci.senprix.campagne.enums.StatutCampagne;
import sn.dci.senprix.campagne.exception.AffectationInvalideException;
import sn.dci.senprix.campagne.exception.CampagneNotFoundException;
import sn.dci.senprix.campagne.mapper.CampagneMapper;
import sn.dci.senprix.campagne.repository.CampagneAgentRepository;
import sn.dci.senprix.campagne.repository.CampagneMarcheRepository;
import sn.dci.senprix.campagne.repository.CampagneProduitRepository;
import sn.dci.senprix.campagne.repository.CampagneRepository;
import sn.dci.senprix.campagne.service.CampagneService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CampagneServiceImpl implements CampagneService {

    private final CampagneRepository campagneRepository;
    private final CampagneAgentRepository campagneAgentRepository;
    private final CampagneMarcheRepository campagneMarcheRepository;
    private final CampagneProduitRepository campagneProduitRepository;
    private final CampagneMapper campagneMapper;
    private final ProduitServiceClient produitServiceClient;
    private final UserServiceClient userServiceClient;

    @Override
    public CampagneResponse creer(CampagneRequest request) {
        validerCoherenceDates(request);
        Campagne campagne = campagneMapper.toEntity(request);
        campagne = campagneRepository.save(campagne);
        return campagneMapper.toResponse(campagne, List.of(), List.of(), List.of());
    }

    @Override
    public CampagneResponse modifier(Long id, CampagneRequest request) {
        validerCoherenceDates(request);
        Campagne campagne = campagneRepository.findById(id)
                .orElseThrow(() -> new CampagneNotFoundException(id));
        campagne.setNom(request.getNom());
        campagne.setDescription(request.getDescription());
        campagne.setDateDebut(request.getDateDebut());
        campagne.setDateFin(request.getDateFin());
        campagne = campagneRepository.save(campagne);
        return construireReponseComplete(campagne);
    }

    @Override
    @Transactional(readOnly = true)
    public CampagneResponse obtenirParId(Long id) {
        Campagne campagne = campagneRepository.findById(id)
                .orElseThrow(() -> new CampagneNotFoundException(id));
        return construireReponseComplete(campagne);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampagneResponse> listerToutes() {
        return campagneRepository.findAll()
                .stream()
                .map(this::construireReponseComplete)
                .toList();
    }

    @Override
    public CampagneResponse affecterAgent(Long campagneId, Long agentId) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new CampagneNotFoundException(campagneId));
        if (campagneAgentRepository.existsByCampagneIdAndAgentId(campagneId, agentId)) {
            throw new AffectationInvalideException("L'agent " + agentId + " est déjà affecté à cette campagne");
        }
        UtilisateurVerificationDto verification = userServiceClient.verifierUtilisateur(agentId);
        if (!verification.isExiste()) {
            throw new AffectationInvalideException("Aucun utilisateur trouvé avec l'identifiant " + agentId);
        }
        if (!"AGENT_COLLECTE".equals(verification.getRole())) {
            throw new AffectationInvalideException("L'utilisateur " + agentId + " n'a pas le rôle AGENT_COLLECTE");
        }
        if (!verification.isActif()) {
            throw new AffectationInvalideException("L'agent " + agentId + " est désactivé");
        }
        CampagneAgent affectation = CampagneAgent.builder().campagne(campagne).agentId(agentId).build();
        campagneAgentRepository.save(affectation);
        return construireReponseComplete(campagne);
    }

    @Override
    public void retirerAgent(Long campagneId, Long agentId) {
        CampagneAgent affectation = campagneAgentRepository
                .findByCampagneIdAndAgentId(campagneId, agentId)
                .orElseThrow(() -> new AffectationInvalideException("L'agent " + agentId + " n'est pas affecté à cette campagne"));
        campagneAgentRepository.delete(affectation);
    }

    @Override
    public CampagneResponse associerMarche(Long campagneId, Long marcheId) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new CampagneNotFoundException(campagneId));
        if (campagneMarcheRepository.existsByCampagneIdAndMarcheId(campagneId, marcheId)) {
            throw new AffectationInvalideException("Le marché " + marcheId + " est déjà associé à cette campagne");
        }
        boolean marcheExiste = produitServiceClient.marcheExiste(marcheId);
        if (!marcheExiste) {
            throw new AffectationInvalideException("Aucun marché trouvé avec l'identifiant " + marcheId);
        }
        CampagneMarche association = CampagneMarche.builder().campagne(campagne).marcheId(marcheId).build();
        campagneMarcheRepository.save(association);
        return construireReponseComplete(campagne);
    }

    @Override
    public void dissocierMarche(Long campagneId, Long marcheId) {
        CampagneMarche association = campagneMarcheRepository
                .findByCampagneIdAndMarcheId(campagneId, marcheId)
                .orElseThrow(() -> new AffectationInvalideException("Le marché " + marcheId + " n'est pas associé à cette campagne"));
        campagneMarcheRepository.delete(association);
    }

    @Override
    public CampagneResponse associerProduit(Long campagneId, Long produitId) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new CampagneNotFoundException(campagneId));
        if (campagneProduitRepository.existsByCampagneIdAndProduitId(campagneId, produitId)) {
            throw new AffectationInvalideException("Le produit " + produitId + " est déjà associé à cette campagne");
        }
        boolean produitExiste = produitServiceClient.produitExiste(produitId);
        if (!produitExiste) {
            throw new AffectationInvalideException("Aucun produit trouvé avec l'identifiant " + produitId);
        }
        CampagneProduit association = CampagneProduit.builder().campagneId(campagneId).produitId(produitId).build();
        campagneProduitRepository.save(association);
        return construireReponseComplete(campagne);
    }

    @Override
    public void dissocierProduit(Long campagneId, Long produitId) {
        CampagneProduit association = campagneProduitRepository
                .findByCampagneIdAndProduitId(campagneId, produitId)
                .orElseThrow(() -> new AffectationInvalideException("Le produit " + produitId + " n'est pas associé à cette campagne"));
        campagneProduitRepository.delete(association);
    }

    @Override
    public CampagneResponse demarrer(Long id) { return changerStatut(id, StatutCampagne.EN_COURS); }

    @Override
    public CampagneResponse terminer(Long id) { return changerStatut(id, StatutCampagne.TERMINEE); }

    @Override
    public CampagneResponse annuler(Long id) { return changerStatut(id, StatutCampagne.ANNULEE); }

    private CampagneResponse changerStatut(Long id, StatutCampagne nouveauStatut) {
        Campagne campagne = campagneRepository.findById(id)
                .orElseThrow(() -> new CampagneNotFoundException(id));
        campagne.setStatut(nouveauStatut);
        campagne = campagneRepository.save(campagne);
        return construireReponseComplete(campagne);
    }

    private CampagneResponse construireReponseComplete(Campagne campagne) {
        List<CampagneAgent> agents = campagneAgentRepository.findByCampagneId(campagne.getId());
        List<CampagneMarche> marches = campagneMarcheRepository.findByCampagneId(campagne.getId());
        List<CampagneProduit> produits = campagneProduitRepository.findByCampagneId(campagne.getId());
        return campagneMapper.toResponse(campagne, agents, marches, produits);
    }

    private void validerCoherenceDates(CampagneRequest request) {
        if (request.getDateFin().isBefore(request.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début");
        }
    }
}
