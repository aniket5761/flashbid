FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/flashbid-0.0.1-SNAPSHOT.jar flashbid.jar
ENTRYPOINT ["java", "-jar", "flashbid.jar"]