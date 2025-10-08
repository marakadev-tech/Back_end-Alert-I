# Utiliser une image Maven officielle avec Java 17
FROM maven:3.9-eclipse-temurin-17 AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers Maven wrapper et pom.xml
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Donner les permissions d'exécution à mvnw
RUN chmod +x mvnw

# Télécharger les dépendances (pour le cache Docker)
RUN ./mvnw dependency:go-offline -B

# Copier le code source
COPY src src

# Builder l'application
RUN ./mvnw clean package -DskipTests

# Image finale légère
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port (Railway définira $PORT)
EXPOSE 8080

# Commande de démarrage
ENTRYPOINT ["java", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]
