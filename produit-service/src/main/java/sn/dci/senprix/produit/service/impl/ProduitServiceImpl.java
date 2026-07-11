package sn.dci.senprix.produit.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dci.senprix.produit.dto.ProduitRequest;
import sn.dci.senprix.produit.dto.ProduitResponse;
import sn.dci.senprix.produit.entity.Produit;
import sn.dci.senprix.produit.enums.StatutEnum;
import sn.dci.senprix.produit.exception.ProduitNotFoundException;
import sn.dci.senprix.produit.mapper.ProduitMapper;
import sn.dci.senprix.produit.repository.ProduitRepository;
import sn.dci.senprix.produit.service.ProduitService;

import java.util.List;

/**
 * Implémentation du service métier de gestion des produits.
 * Génère automatiquement le code produit (format PRD-XXX) à la création.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;
    private final ProduitMapper produitMapper;

    @Override
    public ProduitResponse creer(ProduitRequest request) {
        Produit produit = produitMapper.toEntity(request);
        produit.setCodeProduit(genererCodeProduit());

        Produit sauvegarde = produitRepository.save(produit);
        return produitMapper.toResponse(sauvegarde);
    }

    @Override
    public ProduitResponse modifier(Long id, ProduitRequest request) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException(id));

        produitMapper.updateEntityFromRequest(produit, request);
        Produit sauvegarde = produitRepository.save(produit);
        return produitMapper.toResponse(sauvegarde);
    }

    @Override
    @Transactional(readOnly = true)
    public ProduitResponse obtenirParId(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException(id));
        return produitMapper.toResponse(produit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduitResponse> listerTous() {
        return produitRepository.findAll()
                .stream()
                .map(produitMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduitResponse> listerActifs() {
        return produitRepository.findByStatut(StatutEnum.ACTIF)
                .stream()
                .map(produitMapper::toResponse)
                .toList();
    }

    @Override
    public void desactiver(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException(id));
        produit.setStatut(StatutEnum.INACTIF);
        produitRepository.save(produit);
    }

    @Override
    public void activer(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException(id));
        produit.setStatut(StatutEnum.ACTIF);
        produitRepository.save(produit);
    }

    /**
     * Génère le prochain code produit séquentiel au format PRD-001, PRD-002...
     * basé sur le nombre total de produits existants (y compris inactifs).
     */
    private String genererCodeProduit() {
        long total = produitRepository.count() + 1;
        return String.format("PRD-%03d", total);
    }
}
