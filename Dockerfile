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

# Copier le code source (application-prod.properties sera inclus)
COPY src src

# Builder l'application
RUN ./mvnw clean package -DskipTests

# Vérifier que le JAR a été créé et son contenu
RUN ls -la target/ && \
    echo "Contenu du répertoire target:" && \
    find target -name "*.jar" -type f && \
    echo "Vérification du contenu du JAR:" && \
    jar tf target/Alerti_back-*.jar | grep application.properties

# Image finale légère
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier le JAR depuis l'étape de build (utiliser le wildcard pour être sûr)
COPY --from=build /app/target/*.jar app.jar

# Exposer le port (Railway définira $PORT)
EXPOSE 8080

# Commande de démarrage avec profil production
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=prod -Dserver.port=${PORT:-8080} -jar app.jar"]
