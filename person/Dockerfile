# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy the Maven wrapper and pom
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source files
COPY src ./src

# Package the application
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the actual image
FROM eclipse-temurin:21-jre-alpine

VOLUME /tmp

# Set the working directory
WORKDIR /app

#Set profile to build for k8s, can overide by docker-compose
ENV SPRING_PROFILES_ACTIVE=prod

# Copy the jar from the builder stage
COPY --from=builder /app/target/person-*.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
