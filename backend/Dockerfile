FROM maven:3.8.8-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY libs/ ./libs/
COPY sqlliteadvisory.db ./sqlliteadvisory.db
#RUN mvn clean package -DskipTests
RUN mvn clean package spring-boot:repackage

# Use official Amazon Corretto 11 runtime (pre-built Java runtime)
FROM openjdk:11-jre-slim

# Set working directory inside container
WORKDIR /app

# Copy local libs folder (your custom JARs)
COPY libs/ ./libs/

# Copy SQLite DB into the container
COPY sqlliteadvisory.db ./sqlliteadvisory.db
# Copy the Spring Boot JAR
COPY target/recommendation-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/reports
RUN mkdir -p /app/logs
# Expose port 8080 for Spring Boot
EXPOSE 8082

# Run the application
#ENTRYPOINT ["java", "-jar", "app.jar"]
#ENTRYPOINT ["java", "-Dloader.path=libs/", "-jar", "app.jar"]
ENTRYPOINT ["java", "-Dloader.path=libs/", "-cp", "app.jar", "org.springframework.boot.loader.PropertiesLauncher"]
#ENTRYPOINT ["java", "-cp", "app.jar:libs/*", "tech.algofinserve.recommendation.RecommendationApplication"]
#ENTRYPOINT ["java", "-cp", "app.jar:libs/*", "tech.algofinserve.recommendation.RecommendationApplication"]