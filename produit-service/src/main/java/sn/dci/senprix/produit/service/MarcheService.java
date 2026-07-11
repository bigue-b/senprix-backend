package sn.dci.senprix.produit.service;

import sn.dci.senprix.produit.dto.MarcheRequest;
import sn.dci.senprix.produit.dto.MarcheResponse;

import java.util.List;

/**
 * Contrat du service métier de gestion des marchés.
 */
public interface MarcheService {

    MarcheResponse creer(MarcheRequest request);

    MarcheResponse modifier(Long id, MarcheRequest request);

    MarcheResponse obtenirParId(Long id);

    List<MarcheResponse> listerTous();

    List<MarcheResponse> listerActifs();

    List<MarcheResponse> listerParRegion(String region);

    void desactiver(Long id);

    void activer(Long id);
}
