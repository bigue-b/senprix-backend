# SEN-PRIX — Déploiement Kubernetes local (équivalent de l'architecture AWS EKS/CI-CD/Monitoring)

Ce dossier contient tout ce qu'il faut pour reproduire, **en local et
gratuitement**, la même architecture de déploiement que celle décrite
dans le document de référence (conteneurisation, orchestration,
CI/CD, monitoring, logs) — mais sans compte AWS payant.

## 0. Ce qui est réellement vérifié vs à tester toi-même

J'ai écrit ces fichiers à partir de ton `docker-compose.yml` réel (noms
de services, ports, variables d'environnement identiques), donc la
structure est fiable. Mais je n'ai **pas pu exécuter** ces manifests
sur un vrai cluster Kubernetes depuis mon environnement — teste-les
avant de les présenter comme définitivement fonctionnels.

## 1. Prérequis à installer

- Docker Desktop (ou Docker Engine)
- `kubectl`
- Un cluster Kubernetes local : `minikube` (recommandé pour débuter) ou `kind`
- `helm` n'est pas requis, tout est en manifests YAML bruts ici

```bash
# Installer minikube (exemple macOS/Linux via Homebrew)
brew install minikube kubectl

# Démarrer le cluster local
minikube start --cpus=4 --memory=8192
minikube addons enable ingress
```

## 2. Construire les images Docker de tes 9 microservices

```bash
cd /chemin/vers/senprix-backend
for svc in user-service produit-service campagne-service prix-service \
           alerte-service notif-service rapport-service export-service gateway-service; do
  docker build -t senprix/$svc:latest ./$svc
done

# Rendre ces images visibles à minikube (sans passer par un registre distant)
minikube image load senprix/user-service:latest
# ... répéter pour chaque service, ou utiliser :
eval $(minikube docker-env)  # puis relancer les docker build ci-dessus
```

## 3. Créer les secrets et ConfigMaps qui ne sont PAS dans les fichiers YAML

```bash
kubectl apply -f 00-namespace.yaml

# Le script d'init SQL (déjà existant à la racine de ton projet backend)
kubectl create configmap postgres-init \
  --from-file=init-db.sql \
  -n senprix

# L'export du realm Keycloak (déjà existant dans keycloak-import/)
kubectl create configmap keycloak-realm \
  --from-file=realm-export.json=keycloak-import/realm-export.json \
  -n senprix-keycloak
```

## 4. Déployer l'application

```bash
kubectl apply -f 01-secrets.yaml
kubectl apply -f 02-postgres.yaml
kubectl apply -f 03-keycloak.yaml
kubectl apply -f 04-user-service.yaml
kubectl apply -f 05-produit-service.yaml
kubectl apply -f 06-campagne-service.yaml
kubectl apply -f 07-prix-service.yaml
kubectl apply -f 08-alerte-service.yaml
kubectl apply -f 09-notif-service.yaml
kubectl apply -f 10-rapport-service.yaml
kubectl apply -f 11-export-service.yaml
kubectl apply -f 12-gateway-service.yaml
kubectl apply -f 13-ingress.yaml

# Vérifier que tout démarre correctement
kubectl get pods -n senprix
kubectl get pods -n senprix-keycloak
```

## 5. Déployer le monitoring

```bash
kubectl apply -f monitoring/
kubectl get pods -n senprix-monitoring
```

## 6. Accéder aux interfaces

```bash
# Ajoute dans /etc/hosts (ou C:\Windows\System32\drivers\etc\hosts) :
echo "$(minikube ip) senprix.local keycloak.senprix.local" | sudo tee -a /etc/hosts

# Grafana (admin / admin) :
minikube service grafana -n senprix-monitoring
```

## 7. Étapes complémentaires — voir aussi

- `DEPENDANCES_A_AJOUTER.md` — dépendances Maven à ajouter dans chaque
  service AVANT de reconstruire les images (Prometheus + traçabilité)
- `.github/workflows/backend-ci-cd.yml` — pipeline CI/CD

## 8. Point d'attention sur le CI/CD

Le job `deploy` du pipeline GitHub Actions suppose que le cluster
Kubernetes est **accessible depuis Internet** (via le `KUBE_CONFIG`
fourni). Ce n'est pas le cas d'un cluster minikube tournant uniquement
sur ta machine ! Deux options réalistes :
1. **Runner GitHub auto-hébergé** installé sur ta propre machine (le
   pipeline s'exécute alors localement, avec accès à minikube) ;
2. Ou, plus simple pour un mémoire : **garder le job `deploy` désactivé
   / manuel**, et ne montrer que `build-and-test` comme automatisé —
   c'est déjà un vrai pipeline CI fonctionnel, sans complexité inutile.

## 9. Ce qui a été volontairement simplifié par rapport au document de référence

| Document de référence (AWS) | Ici (local) | Raison |
|---|---|---|
| Amazon EKS | Kubernetes local (minikube/kind) | Pas de compte AWS |
| Amazon ECR | Docker Hub (ou images chargées directement dans minikube) | Gratuit |
| Amazon RDS | PostgreSQL en StatefulSet (déjà conteneurisé) | Inchangé, déjà existant |
| Amazon S3 | Non repris | SEN-PRIX ne stocke pas de fichiers (CV, contrats...) |
| Amazon DocumentDB | Non repris | Aucune donnée non-relationnelle dans SEN-PRIX |
| AWS Secrets Manager | Kubernetes Secrets | Équivalent natif gratuit |
| AWS CloudWatch | Prometheus + Grafana (déjà prévu) | Remplace directement |
| ELK Stack | Loki + Promtail | Plus léger, mieux adapté à un usage local |
| Spring Sleuth | Micrometer Tracing | Sleuth est déprécié depuis Spring Boot 3.x |
