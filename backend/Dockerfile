# Pink Door Java backend (Spring Boot 3, Java 17) - Maozi Cloud Dockerfile.
# Build with Maven, run the jar. The app listens on 0.0.0.0:8080 (Spring Boot
# binds all interfaces by default; server.port is 8080 in application.yml).

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
