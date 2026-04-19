# Stage 1: Build the Spring Boot application 
# Use Maven with JDK 21 to compile the application
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy only pom.xml to leverage Docker layer caching for dependencies
COPY pom.xml .

# Download all project dependencies cached unless pom.xml changes
RUN mvn dependency:go-offline

# Copy the application source code
COPY src ./src

# Compile and package the application into a JAR file 
RUN mvn clean package -DskipTests


#  Stage 2: Create a runtime image 
# Use a JDK runtime image to run the application
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory for the runtime container
WORKDIR /app

# Copy the built JAR from the build stage into the runtime image
COPY --from=build /app/target/*.jar app.jar

# Define the command to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]