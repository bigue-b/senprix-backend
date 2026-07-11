-- Ce script est exécuté automatiquement par le conteneur PostgreSQL
-- UNIQUEMENT au tout premier démarrage (volume vide).
-- Il crée les 7 bases nécessaires ; chaque microservice se connecte ensuite
-- à SA base via son propre Liquibase, qui crée les tables à l'intérieur.

CREATE DATABASE user_db;
CREATE DATABASE produit_db;
CREATE DATABASE campagne_db;
CREATE DATABASE prix_db;
CREATE DATABASE alerte_db;
CREATE DATABASE notif_db;
CREATE DATABASE rapport_db;
CREATE DATABASE keycloak;
