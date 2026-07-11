package sn.dci.senprix.notif.service;

import sn.dci.senprix.notif.dto.AbonnementRequest;
import sn.dci.senprix.notif.dto.AbonnementResponse;

import java.util.List;

public interface AbonnementService {

    AbonnementResponse creer(String citoyenId, String citoyenEmail, AbonnementRequest request);

    List<AbonnementResponse> listerParCitoyen(String citoyenId);

    void supprimer(Long id, String citoyenId);
}
