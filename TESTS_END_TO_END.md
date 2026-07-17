# Test end-to-end : flux prix suspect → alerte → notification

Date : 2026-07-17
Environnement : stack complète via `docker-compose up --build` (12 conteneurs, tous `Up`)

## Contexte

Suite au fix du crash JVM de `produit-service` (SIGSEGV dans
`PhaseIdealLoop::Dominators`, résolu par l'ajout de
`-XX:TieredStopAtLevel=1` dans `produit-service/Dockerfile`), validation
du flux métier complet de bout en bout :

1. Un agent de collecte crée un relevé de prix via l'API REST de `prix-service`.
2. Si l'écart avec la moyenne des relevés existants (même produit/marché)
   dépasse 50 %, `prix-service` publie un message sur RabbitMQ
   (exchange `prix.suspect.exchange`, routing key `prix.suspect`).
3. `alerte-service` consomme ce message depuis la file `alerte.queue`,
   calcule le niveau de gravité (`FAIBLE` / `MOYENNE` / `ELEVEE` selon
   les seuils de `SeuilAlertesProperties` : 100 % / 200 %) et crée une
   alerte en base.
4. `alerte-service` notifie `notif-service`.

Données de référence utilisées (déjà présentes en base) :
- Produit id=1 : « Riz brisé 25kg », marché id=1 : « Marché Sandaga »
- Campagne id=1 : « campagne juillet 2026 » (EN_COURS), agent id=1 affecté
- Agent de test : `f@dci.sn` (utilisateur `ba fatou`, rôle `AGENT_COLLECTE`
  dans Keycloak, realm `senprix`)
- Aucun relevé préexistant pour le couple produit=1/marché=1 (moyenne à zéro
  avant le test)

## 1. Obtention du token JWT

Mot de passe de test réinitialisé via l'API admin Keycloak (realm `master`,
`admin`/`admin`), puis token récupéré par password grant sur le client
public `senprix-app` (pas de secret requis) :

```bash
# Token admin (realm master) pour réinitialiser le mot de passe du compte de test
ADMIN_TOKEN=$(curl -s -X POST "http://localhost:8080/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" -d "client_id=admin-cli" \
  -d "username=admin" -d "password=admin" \
  | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

curl -s -X PUT "http://localhost:8080/admin/realms/senprix/users/2b1cec33-da4d-40b1-b317-c4d4188fbe89/reset-password" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"password","value":"Test1234!","temporary":false}'
# => HTTP 204

# Token agent de collecte (utilisé pour les appels API)
AGENT_TOKEN=$(curl -s -X POST "http://localhost:8080/realms/senprix/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" -d "client_id=senprix-app" \
  -d "username=f@dci.sn" -d "password=Test1234!" \
  | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)
```

## 2. Requêtes curl — création des relevés de prix

### Relevé 1 (référence, établit la moyenne)

```bash
curl -s -X POST "http://localhost:8084/api/agent/prix" \
  -H "Authorization: Bearer $AGENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "produitId": 1,
    "marcheId": 1,
    "campagneId": 1,
    "agentId": 1,
    "montant": 1000,
    "unite": "Sac 25kg",
    "dateReleve": "2026-07-17"
  }'
```

Réponse (`HTTP 201`) :

```json
{"id":7,"produitId":1,"marcheId":1,"campagneId":1,"agentId":1,"montant":1000,"unite":"Sac 25kg","dateReleve":"2026-07-17","statut":"VALIDE","commentaire":null,"dateCreation":"2026-07-17T16:16:15.259232"}
```

### Relevé 2 (suspect, écart de +250 % vs moyenne)

```bash
curl -s -X POST "http://localhost:8084/api/agent/prix" \
  -H "Authorization: Bearer $AGENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "produitId": 1,
    "marcheId": 1,
    "campagneId": 1,
    "agentId": 1,
    "montant": 3500,
    "unite": "Sac 25kg",
    "dateReleve": "2026-07-17"
  }'
```

Réponse (`HTTP 201`) :

```json
{"id":8,"produitId":1,"marcheId":1,"campagneId":1,"agentId":1,"montant":3500,"unite":"Sac 25kg","dateReleve":"2026-07-17","statut":"SUSPECT","commentaire":null,"dateCreation":"2026-07-17T16:17:14.901306"}
```

Le statut `SUSPECT` confirme que `prix-service` a détecté l'écart
(> 50 % de la moyenne de 1000) et déclenché la publication RabbitMQ.

## 3. Résultat observé — logs `alerte-service`

```
docker-compose logs --since=5m alerte-service
```

```
2026-07-17T16:17:15.127Z INFO ... AlerteEventListener : Message reçu depuis la file 'alerte.queue' : prix suspect pour le produit 1 (prixId=8, montant=3500, moyenne=1000.0)
2026-07-17T16:17:20.180Z INFO ... NotifServiceClient : Notification envoyée avec succès pour l'alerte 2
```

Le message a été correctement consommé et l'alerte a déclenché l'envoi
d'une notification vers `notif-service`.

## 4. Résultat observé — base de données `alerte_db`

```sql
SELECT * FROM alerte ORDER BY id DESC LIMIT 1;
```

| id | prix_id | produit_id | marche_id | montant | montant_moyen | ecart_pourcentage | niveau_gravite | statut   |
|----|---------|------------|-----------|---------|---------------|--------------------|-----------------|----------|
| 2  | 8       | 1          | 1         | 3500.00 | 1000.00       | 250.00             | ELEVEE          | NOUVELLE |

L'écart de 250 % dépasse le seuil `seuil-eleve: 200` configuré dans
`alerte-service/src/main/resources/application.yml`
(`SeuilAlertesProperties`), ce qui explique la gravité `ELEVEE`.

## Conclusion

Le flux `prix-service` → RabbitMQ (`prix.suspect.exchange` /
`alerte.queue`) → `alerte-service` → `notif-service` fonctionne de bout
en bout sur la stack reconstruite avec le fix JVM
(`-XX:TieredStopAtLevel=1`). Aucune régression constatée.
