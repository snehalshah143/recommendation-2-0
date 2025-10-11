# Use official OpenJDK 11 image
FROM openjdk:11-jdk-slim

# Set working directory inside the container
WORKDIR /app

# Copy pom.xml and download dependencies first (caching layer)
COPY pom.xml .
RUN apt-get update && apt-get install -y maven
RUN mvn dependency:go-offline -B

# Copy the entire backend source code
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# SQLite DB (if you want it inside container)
# COPY sqlliteadvisory.db .

# Expose port
EXPOSE 8082

# Run the jar
CMD ["java", "-jar", "target/recommendation-0.0.1-SNAPSHOT.jar"]