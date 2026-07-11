# =====================================================================
# Dockerfile — à copier TEL QUEL à la racine de CHAQUE microservice
# (user-service/Dockerfile, produit-service/Dockerfile, etc.)
# Identique pour les 9 services : ils utilisent tous Java 17 + Maven.
# =====================================================================

# --- Étape 1 : compilation du projet avec Maven ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Télécharge les dépendances Maven en cache (accélère les futurs builds)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.test.skip=true -B

# --- Étape 2 : image finale, légère, avec juste le .jar ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Le nom du jar correspond à <artifactId>-<version>.jar (ex: prix-service-1.0.0.jar)
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
