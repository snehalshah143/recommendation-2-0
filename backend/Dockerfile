# Stage 1: Build the app using Maven
FROM maven:3.9.4-amazoncorretto-11 AS build

WORKDIR /app

# Copy pom.xml first to leverage caching
COPY pom.xml .

# Download dependencies offline
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Package the jar
RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM amazoncorretto:11

WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/target/recommendation-0.0.1-SNAPSHOT.jar app.jar

# Copy SQLite DB into the container
COPY sqlliteadvisory.db ./sqlliteadvisory.db

# Expose the port
EXPOSE 8082

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
