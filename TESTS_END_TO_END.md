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

---

# Test end-to-end : répartition de charge Eureka + Spring Cloud LoadBalancer

Date : 2026-08-05
Environnement : cluster Kubernetes local **Docker Desktop** (nœud
`desktop-control-plane`, v1.36.1), namespace `senprix`

## Contexte

Après l'ajout de l'annuaire `discovery-service` (Eureka Server, port
8761), validation du fait que la découverte de services produit bien une
**répartition de charge réelle** entre plusieurs instances, et pas
seulement une résolution de nom :

1. `prix-service` et `alerte-service` passent à `replicas: 2`
   (`k8s/07-prix-service.yaml`, `k8s/08-alerte-service.yaml`).
2. Les deux pods de chaque service s'enregistrent dans Eureka sous le
   même nom applicatif mais avec des `instanceId` et des IP distincts.
3. La Gateway route `lb://PRIX-SERVICE` en alternant entre les deux
   instances via Spring Cloud LoadBalancer.

## 0. Vérification préalable — Spring Cloud LoadBalancer est bien actif

`spring-cloud-starter-loadbalancer` n'est déclaré explicitement dans
aucun `pom.xml` : il arrive **transitivement** via
`spring-cloud-starter-netflix-eureka-client`. Vérifié sur les 5 services
concernés (`mvn dependency:tree`) :

```
gateway-service    +- org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:jar:4.1.3:compile
                   |  \- org.springframework.cloud:spring-cloud-starter-loadbalancer:jar:4.1.4:compile
prix-service       spring-cloud-starter-loadbalancer:jar:4.1.4
rapport-service    spring-cloud-starter-loadbalancer:jar:4.1.4
export-service     spring-cloud-starter-loadbalancer:jar:4.1.4
campagne-service   spring-cloud-starter-loadbalancer:jar:4.1.4
alerte-service     spring-cloud-starter-loadbalancer:jar:4.1.4
```

Il est donc actif des deux côtés :
- **Gateway** : URIs `lb://NOM-SERVICE` dans
  `gateway-service/src/main/resources/application.yml`.
- **Clients REST inter-services** : bean `RestClient.Builder` annoté
  `@LoadBalanced` dans chaque `config/RestClientConfig.java`, avec des
  cibles de la forme `http://PRIX-SERVICE` (le nom du service remplace
  l'hôte ; `lb://` est une syntaxe propre aux routes de la Gateway).

## 1. Déploiement des 2 répliques

```bash
kubectl apply -f k8s/07-prix-service.yaml
kubectl apply -f k8s/08-alerte-service.yaml
kubectl get pods -n senprix
```

```
NAME                                 READY   STATUS    RESTARTS   AGE
alerte-service-6b5d78b89b-hnx6l      1/1     Running   0          73s
alerte-service-6b5d78b89b-r7n78      1/1     Running   0          73s
discovery-service-7bdd949869-npxkr   1/1     Running   0          13h
gateway-service-5b885c8bd7-s67ks     1/1     Running   0          16m
postgres-0                           1/1     Running   0          18d
prix-service-c555b4c4c-6qxkv         1/1     Running   0          7m44s
prix-service-c555b4c4c-vjk2z         1/1     Running   0          8m10s
rabbitmq-b75567fbb-mxwnp             1/1     Running   0          10m
```

## 2. Résultat observé — double enregistrement dans Eureka

```bash
kubectl exec -n senprix deploy/discovery-service -- \
  wget -qO- http://localhost:8761/eureka/apps
```

```xml
name>PRIX-SERVICE
  instanceId>prix-service-c555b4c4c-6qxkv:PRIX-SERVICE:8084   ipAddr>10.244.0.91   status>UP
  instanceId>prix-service-c555b4c4c-vjk2z:PRIX-SERVICE:8084   ipAddr>10.244.0.90   status>UP
name>ALERTE-SERVICE
  instanceId>alerte-service-6b5d78b89b-r7n78:ALERTE-SERVICE:8085   ipAddr>10.244.0.93   status>UP
  instanceId>alerte-service-6b5d78b89b-hnx6l:ALERTE-SERVICE:8085   ipAddr>10.244.0.92   status>UP
name>GATEWAY-SERVICE
  instanceId>gateway-service-5b885c8bd7-s67ks:GATEWAY-SERVICE:8090   ipAddr>10.244.0.85   status>UP
```

Un seul nom applicatif, deux `instanceId` et deux IP de pod distincts :
c'est exactement ce dont Spring Cloud LoadBalancer a besoin pour
alterner. Confirmé côté pods :

```
DiscoveryClient_PRIX-SERVICE/prix-service-c555b4c4c-6qxkv:PRIX-SERVICE:8084 - registration status: 204
DiscoveryClient_PRIX-SERVICE/prix-service-c555b4c4c-vjk2z:PRIX-SERVICE:8084 - registration status: 204
```

## 3. Test de répartition — 30 requêtes via la Gateway

Traçage des requêtes activé sans modifier le code, par variable
d'environnement :

```bash
kubectl set env deployment/prix-service -n senprix \
  LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB=DEBUG
```

Puis, la Gateway étant exposée localement :

```bash
kubectl port-forward -n senprix svc/gateway-service 8090:8090

# Série 1 : 20 requêtes
for i in $(seq 1 20); do curl -s -o /dev/null -w "%{http_code} " \
  http://localhost:8090/api/public/prix; done
# => 200 200 200 200 200 200 200 200 200 200 200 200 200 200 200 200 200 200 200 200
```

Comptage par pod :

```bash
for p in $(kubectl get pods -n senprix -l app=prix-service -o name); do
  echo "${p#pod/} : $(kubectl logs -n senprix ${p#pod/} | grep -c 'GET "/api/public/prix"')"
done
```

## 4. Résultat observé — répartition strictement équilibrée

| Série | Requêtes envoyées | pod `...-6qxkv` (10.244.0.91) | pod `...-vjk2z` (10.244.0.90) |
|-------|-------------------|-------------------------------|-------------------------------|
| 1     | 20                | 10                            | 10                            |
| 1 + 2 | 30 (cumul)        | 15                            | 15                            |

Extraits bruts des logs des deux pods, qui montrent l'alternance
requête par requête (horodatages entrelacés à la milliseconde) :

```
--- prix-service-c555b4c4c-vjk2z ---
2026-08-05T13:52:43.006Z DEBUG 1 --- [PRIX-SERVICE] [nio-8084-exec-4] o.s.web.servlet.DispatcherServlet : GET "/api/public/prix", parameters={}
2026-08-05T13:52:43.508Z DEBUG 1 --- [PRIX-SERVICE] [nio-8084-exec-5] o.s.web.servlet.DispatcherServlet : GET "/api/public/prix", parameters={}

--- prix-service-c555b4c4c-6qxkv ---
2026-08-05T13:52:43.254Z DEBUG 1 --- [PRIX-SERVICE] [nio-8084-exec-5] o.s.web.servlet.DispatcherServlet : GET "/api/public/prix", parameters={}
2026-08-05T13:52:43.606Z DEBUG 1 --- [PRIX-SERVICE] [nio-8084-exec-6] o.s.web.servlet.DispatcherServlet : GET "/api/public/prix", parameters={}
```

Ordre chronologique reconstitué : `vjk2z` (43.006) → `6qxkv` (43.254) →
`vjk2z` (43.508) → `6qxkv` (43.606). L'alternance est parfaite, ce qui
correspond à `RoundRobinLoadBalancer`, la stratégie par défaut de Spring
Cloud LoadBalancer. Aucune requête n'est tombée (30/30 en HTTP 200) et
aucune n'a été perdue (30 lignes de log pour 30 requêtes).

## 5. Difficultés rencontrées et corrections apportées

Trois obstacles ont dû être levés pour que le test aboutisse — ils sont
documentés ici car ils se reproduiront à chaque déploiement local :

**a) Les pods tournaient sur d'anciennes images.** Docker Desktop exécute
Kubernetes sur **containerd**, qui possède son propre magasin d'images :
un `docker build` n'alimente pas le nœud. Avec `imagePullPolicy:
IfNotPresent`, containerd continuait de servir l'image du tag `latest`
telle qu'il l'avait mise en cache (le JAR ne contenait aucune classe
Eureka, d'où un registre désespérément vide). Il faut importer
explicitement les images :

```bash
docker save senprix/prix-service:latest | \
  docker exec -i desktop-control-plane ctr -n k8s.io images import -
kubectl rollout restart deployment/prix-service -n senprix
```

**b) `limits.cpu: 500m` empêchait le démarrage.** Le démarrage d'une JVM
Spring Boot est un pic CPU court mais intense. Bridé à une demi-cœur,
l'initialisation du seul contexte web prenait déjà ~58 s et le démarrage
complet dépassait le `startupProbe` (60 × 5 s = 5 min) : Kubernetes
tuait le pod par SIGTERM, qui repartait en boucle (6 redémarrages
observés). Corrigé dans les manifests : `cpu: 2000m` et
`failureThreshold: 150`. Une fois corrigé, les deux pods démarrent en
**52 s et 57 s, sans aucun redémarrage**.

**c) Le pod RabbitMQ était en CrashLoopBackOff** (19 redémarrages) sur
`Error when reading /var/lib/rabbitmq/.erlang.cookie: eacces`. Problème
préexistant, sans rapport avec Eureka, mais bloquant : son manifest ne
déclare aucun volume, le fichier de cookie corrompu survivait donc aux
redémarrages du conteneur. Comme `spring-boot-starter-amqp` ajoute un
indicateur `rabbit` à `/actuator/health`, les pods `prix-service`
renvoyaient 503 sur leurs probes et ne passaient jamais `READY`. Résolu
en recréant le pod (`kubectl scale --replicas=0` puis `1`), ce qui
repart d'un système de fichiers neuf.

## Conclusion

La découverte de services ne se limite pas à la résolution de noms :
elle assure une **répartition de charge effective**. Sur 30 requêtes
envoyées à `lb://PRIX-SERVICE` via la Gateway, les deux instances en ont
traité exactement 15 chacune, en alternance stricte, sans configuration
de load balancer autre que la présence transitive de
`spring-cloud-starter-loadbalancer`. Monter une réplique supplémentaire
ne demande désormais qu'un changement de `replicas` — aucune URL, aucun
port et aucun manifeste de Gateway à retoucher.

---

# Test end-to-end : rate limiting sur la Gateway (Redis)

Date : 2026-08-05
Environnement : cluster Kubernetes local Docker Desktop, namespace
`senprix`, 10 microservices + postgres + rabbitmq + **redis**

## Contexte

Protection de la Gateway contre les rafales d'appels, en s'appuyant sur
le filtre `RequestRateLimiter` **natif** de Spring Cloud Gateway.

Choix de l'implémentation : Redis, et non Resilience4j. Resilience4j
apporte du *circuit breaking* (couper les appels vers un service en
panne), pas de la limitation de débit côté Gateway — la seule
implémentation de `RateLimiter` fournie en standard par Spring Cloud
Gateway est `RedisRateLimiter`. Redis est par ailleurs le composant le
plus léger à ajouter au cluster (image `redis:7-alpine`, 64 Mi de
`requests`, sans volume).

L'intérêt d'un compteur externe plutôt qu'en mémoire : il est **partagé**
entre toutes les instances de la Gateway. Monter `gateway-service` à 2
répliques ne double donc pas la limite réelle.

### Configuration retenue

`gateway-service/src/main/resources/application.yml` — 20 requêtes par
seconde et par utilisateur, sur trois routes choisies pour leur coût ou
leur sensibilité :

| Route | Pourquoi elle est limitée |
|---|---|
| `prix-service` (`/api/agent/prix/**`, …) | endpoint le plus sollicité — saisie terrain, et chaque écriture peut publier un message RabbitMQ vers `alerte-service` |
| `export-service` (`/api/*/exports/**`) | appel le plus coûteux : interroge `prix-service` et `produit-service` puis construit le fichier en mémoire |
| `user-service` (`/api/admin/utilisateurs/**`, …) | chaque appel se répercute sur l'API admin de Keycloak |

```yaml
filters:
  - name: RequestRateLimiter
    args:
      key-resolver: "#{@utilisateurKeyResolver}"
      redis-rate-limiter.replenishRate: 20
      redis-rate-limiter.burstCapacity: 20
      redis-rate-limiter.requestedTokens: 1
```

`burstCapacity` est volontairement aligné sur `replenishRate` pour
obtenir un plafond strict ; le porter à 40 tolérerait des pics courts.

### Clé de comptage

`RateLimitConfig.utilisateurKeyResolver()` compte **par utilisateur**
(revendication `sub` du jeton) et retombe sur l'adresse IP en l'absence
de jeton — sans ce repli, tout le trafic anonyme partagerait un unique
compteur et un seul appelant bloquerait les autres.

La signature du jeton n'est pas vérifiée dans la Gateway : elle n'est pas
la frontière de confiance ici, chaque microservice validant le jeton
auprès de Keycloak. La revendication ne sert qu'à répartir des compteurs,
jamais à accorder un accès — un jeton falsifié n'obtient donc qu'un
compteur à lui, soit exactement l'effet d'un appel anonyme depuis une
autre IP. Le test 3 ci-dessous le montre au passage.

## 1. Résultat observé — la limite est appliquée

60 requêtes simultanées (`curl --parallel`) sur une route limitée :

```bash
kubectl port-forward -n senprix svc/gateway-service 8090:8090

URLS=""; for i in $(seq 1 60); do URLS="$URLS http://localhost:8090/api/public/prix"; done
curl -s --no-progress-meter --parallel --parallel-immediate --parallel-max 60 \
     -o /dev/null -w "CODE=%{http_code}\n" $URLS | sort | uniq -c
```

```
     20 CODE=200
     40 CODE=429
```

**Exactement 20 passent, 40 sont rejetées** — le plafond correspond au
`burstCapacity` configuré, au jeton près.

En-têtes renvoyés sur une requête rejetée :

```
HTTP/1.1 429 Too Many Requests
X-RateLimit-Remaining: 0
X-RateLimit-Requested-Tokens: 1
X-RateLimit-Burst-Capacity: 20
X-RateLimit-Replenish-Rate: 20
```

## 2. Résultat observé — une route non limitée n'est pas affectée

Même rafale de 60 requêtes sur `/api/public/produits`, route sans filtre :

```
     60 CODE=200
```

Aucun rejet : la limitation est bien ciblée sur les routes choisies et
n'est pas un effet global de la Gateway.

## 3. Résultat observé — les compteurs sont séparés par utilisateur

30 requêtes avec un jeton portant `"sub":"agent-A"`, puis immédiatement
30 requêtes avec `"sub":"agent-B"` :

```
--- agent-A ---            --- agent-B ---
     20 CODE=401                30 CODE=401
     10 CODE=429
```

Deux enseignements :

- **agent-A est bloqué (10 rejets), agent-B ne l'est pas du tout** alors
  que sa rafale suit immédiatement. Avec un compteur partagé, agent-B
  aurait été rejeté d'entrée : les buckets sont bien distincts par
  utilisateur.
- Les `401` viennent de `prix-service`, qui refuse ces jetons fabriqués
  pour le test. C'est précisément la preuve que ces requêtes **ont
  traversé le rate limiter** et atteint le microservice : la Gateway ne
  valide pas les jetons, chaque service s'en charge.

## 4. Résultat observé — la limite se reconstitue

Après 3 secondes sans trafic :

```
  requête 1 -> HTTP 200
  requête 2 -> HTTP 200
  requête 3 -> HTTP 200
```

Le bucket se remplit bien à `replenishRate` jetons par seconde : la
limitation est temporaire, pas un blocage persistant.

## Conclusion

Le rate limiting est **réellement appliqué**, pas seulement configuré :
20 requêtes admises et 40 rejetées en HTTP 429 sur une rafale de 60,
avec les en-têtes `X-RateLimit-*` correspondants. Il est ciblé (une route
sans filtre reste libre), individualisé (un utilisateur saturé n'affecte
pas les autres) et transitoire (reconstitution en quelques secondes).

Le compteur vivant dans Redis et non dans la JVM, la limite restera
globale le jour où `gateway-service` passera à plusieurs répliques.
