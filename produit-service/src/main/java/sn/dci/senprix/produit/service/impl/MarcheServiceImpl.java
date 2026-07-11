package sn.dci.senprix.produit.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dci.senprix.produit.dto.MarcheRequest;
import sn.dci.senprix.produit.dto.MarcheResponse;
import sn.dci.senprix.produit.entity.Marche;
import sn.dci.senprix.produit.enums.StatutEnum;
import sn.dci.senprix.produit.exception.MarcheNotFoundException;
import sn.dci.senprix.produit.mapper.MarcheMapper;
import sn.dci.senprix.produit.repository.MarcheRepository;
import sn.dci.senprix.produit.service.MarcheService;

import java.util.List;

/**
 * Implémentation du service métier de gestion des marchés.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MarcheServiceImpl implements MarcheService {

    private final MarcheRepository marcheRepository;
    private final MarcheMapper marcheMapper;

    @Override
    public MarcheResponse creer(MarcheRequest request) {
        Marche marche = marcheMapper.toEntity(request);
        Marche sauvegarde = marcheRepository.save(marche);
        return marcheMapper.toResponse(sauvegarde);
    }

    @Override
    public MarcheResponse modifier(Long id, MarcheRequest request) {
        Marche marche = marcheRepository.findById(id)
                .orElseThrow(() -> new MarcheNotFoundException(id));

        marcheMapper.updateEntityFromRequest(marche, request);
        Marche sauvegarde = marcheRepository.save(marche);
        return marcheMapper.toResponse(sauvegarde);
    }

    @Override
    @Transactional(readOnly = true)
    public MarcheResponse obtenirParId(Long id) {
        Marche marche = marcheRepository.findById(id)
                .orElseThrow(() -> new MarcheNotFoundException(id));
        return marcheMapper.toResponse(marche);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarcheResponse> listerTous() {
        return marcheRepository.findAll()
                .stream()
                .map(marcheMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarcheResponse> listerActifs() {
        return marcheRepository.findByStatut(StatutEnum.ACTIF)
                .stream()
                .map(marcheMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarcheResponse> listerParRegion(String region) {
        return marcheRepository.findByRegionIgnoreCase(region)
                .stream()
                .map(marcheMapper::toResponse)
                .toList();
    }

    @Override
    public void desactiver(Long id) {
        Marche marche = marcheRepository.findById(id)
                .orElseThrow(() -> new MarcheNotFoundException(id));
        marche.setStatut(StatutEnum.INACTIF);
        marcheRepository.save(marche);
    }

    @Override
    public void activer(Long id) {
        Marche marche = marcheRepository.findById(id)
                .orElseThrow(() -> new MarcheNotFoundException(id));
        marche.setStatut(StatutEnum.ACTIF);
        marcheRepository.save(marche);
    }
}
