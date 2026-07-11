# Intégrer Docker dans SEN-PRIX — Guide pas à pas

## Ce que contient ce zip

```
senprix-docker/
├── docker-compose.yml       ← le fichier "chef d'orchestre" (à mettre à la racine)
├── init-db.sql              ← crée les 7 bases automatiquement
├── keycloak-import/
│   └── realm-export.json    ← ton realm senprix, importé automatiquement
├── user-service/Dockerfile
├── produit-service/Dockerfile
├── campagne-service/Dockerfile
├── prix-service/Dockerfile
├── alerte-service/Dockerfile
├── notif-service/Dockerfile
├── rapport-service/Dockerfile
├── export-service/Dockerfile
└── gateway-service/Dockerfile
```

## Prérequis

Installer **Docker Desktop** (Windows) : https://www.docker.com/products/docker-desktop/
Après installation, lance Docker Desktop et attends qu'il affiche "Engine running".

## Étape 1 — Copier les Dockerfiles dans ton vrai projet

Pour CHAQUE service, copie le fichier `Dockerfile` correspondant depuis ce zip
directement à la racine du dossier du service dans ton projet réel.

Exemple concret avec `prix-service` :

```
Copie :  senprix-docker/prix-service/Dockerfile
Vers  :  C:\Users\HP\Downloads\SENPRIX\senprix-backend\senprix-backend\prix-service\Dockerfile
```

Répète pour les 9 services. Le fichier `Dockerfile` doit être au même niveau que
le fichier `pom.xml` de chaque service (pas dans `src/`).

## Étape 2 — Copier `docker-compose.yml`, `init-db.sql` et `keycloak-import/`

Ces éléments vont à la RACINE de ton projet, au même niveau que tous les
dossiers de services :

```
C:\Users\HP\Downloads\SENPRIX\senprix-backend\senprix-backend\
├── docker-compose.yml       ← ICI (nouveau)
├── init-db.sql              ← ICI (nouveau)
├── keycloak-import/         ← ICI (nouveau, dossier complet avec le fichier dedans)
├── user-service/
├── produit-service/
├── campagne-service/
├── prix-service/
├── alerte-service/
├── notif-service/
├── rapport-service/
├── export-service/
└── gateway-service/
```

## Étape 3 — Le realm Keycloak (déjà fait, rien à faire)

✅ Ton realm `senprix` (rôles ADMIN/AGENT_COLLECTE/CONSOMMATEUR, clients
`senprix-admin-client` et `senprix-app`, avec son secret) est déjà inclus
dans `keycloak-import/realm-export.json` et sera **importé automatiquement**
au premier démarrage du conteneur Keycloak. Tu n'as rien à reconfigurer
manuellement.

Vérifie juste que le dossier `keycloak-import/` (avec `realm-export.json`
dedans) est bien copié à la racine de ton projet, au même niveau que
`docker-compose.yml`.

## Étape 4 — Fermer tes services actuels

Si tu as des services qui tournent déjà manuellement dans IntelliJ (Run),
**arrête-les tous** avant de lancer Docker (sinon conflit de ports).

Ferme aussi ton PostgreSQL local s'il tourne déjà sur le port 5432 (sinon Docker
ne pourra pas démarrer son propre PostgreSQL sur ce même port).

## Étape 5 — Lancer toute la stack

Ouvre un terminal (PowerShell ou cmd) à la racine du projet, où se trouve
`docker-compose.yml`, puis tape :

```bash
docker-compose up --build
```

**Première fois** : ça va prendre du temps (5-15 minutes) — Docker télécharge les
images de base et compile chacun de tes 9 services avec Maven. Les fois suivantes
seront beaucoup plus rapides.

Tu vas voir défiler beaucoup de logs mélangés (tous les services démarrent en
même temps). C'est normal.

## Étape 6 — Vérifier que tout tourne

Ouvre un **autre** terminal (laisse le premier tourner) et tape :

```bash
docker ps
```

Tu dois voir 10 conteneurs actifs (9 services + 1 postgres), plus Keycloak.

Teste ensuite dans ton navigateur :
```
http://localhost:8090/api/public/produits
```
(à travers le Gateway, port 8090)

## Arrêter proprement

Dans le terminal où `docker-compose up` tourne, fais **Ctrl+C**, puis :
```bash
docker-compose down
```

## En cas de problème

Colle-moi directement le message d'erreur affiché dans le terminal — comme on
a fait pour le souci Liquibase, je pourrai te dire précisément quoi corriger.

Erreurs fréquentes à anticiper :
- **"port already allocated"** → un de tes services tourne encore sur ce port (via IntelliJ) ou PostgreSQL local est encore actif → arrête-le d'abord
- **Erreur de connexion Keycloak** → le realm `senprix` n'est pas encore configuré dans le Keycloak conteneurisé (voir Étape 3)
