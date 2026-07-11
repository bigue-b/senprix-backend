package sn.dci.senprix.notif.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dci.senprix.notif.dto.AbonnementRequest;
import sn.dci.senprix.notif.dto.AbonnementResponse;
import sn.dci.senprix.notif.entity.Abonnement;
import sn.dci.senprix.notif.exception.AbonnementDejaExistantException;
import sn.dci.senprix.notif.exception.AbonnementNotFoundException;
import sn.dci.senprix.notif.mapper.AbonnementMapper;
import sn.dci.senprix.notif.repository.AbonnementRepository;
import sn.dci.senprix.notif.service.AbonnementService;

import java.util.List;

/**
 * Implémentation du service métier de gestion des abonnements citoyens
 * aux alertes de prix. citoyenId/citoyenEmail proviennent toujours du
 * token JWT (extraits dans le contrôleur), jamais du corps de la
 * requête, pour garantir qu'un citoyen ne gère que ses propres
 * abonnements.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AbonnementServiceImpl implements AbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final AbonnementMapper abonnementMapper;

    @Override
    public AbonnementResponse creer(String citoyenId, String citoyenEmail, AbonnementRequest request) {
        boolean dejaAbonne = abonnementRepository.existsByCitoyenIdAndProduitIdAndMarcheId(
                citoyenId, request.getProduitId(), request.getMarcheId());

        if (dejaAbonne) {
            throw new AbonnementDejaExistantException(request.getProduitId(), request.getMarcheId());
        }

        Abonnement abonnement = Abonnement.builder()
                .citoyenId(citoyenId)
                .citoyenEmail(citoyenEmail)
                .produitId(request.getProduitId())
                .marcheId(request.getMarcheId())
                .canal(request.getCanal())
                .build();

        abonnement = abonnementRepository.save(abonnement);
        return abonnementMapper.toResponse(abonnement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbonnementResponse> listerParCitoyen(String citoyenId) {
        return abonnementRepository.findByCitoyenId(citoyenId).stream()
                .map(abonnementMapper::toResponse)
                .toList();
    }

    @Override
    public void supprimer(Long id, String citoyenId) {
        Abonnement abonnement = abonnementRepository.findById(id)
                .filter(a -> a.getCitoyenId().equals(citoyenId))
                .orElseThrow(() -> new AbonnementNotFoundException(id));

        abonnementRepository.delete(abonnement);
    }
}
