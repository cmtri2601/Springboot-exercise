# -------- Stage 1: Build the application --------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and config from the monorepo root
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy the common module (00-common)
COPY 00-common/pom.xml 00-common/pom.xml
COPY 00-common/src     00-common/src

# Copy the person-service module (this module)
COPY 01-person-service/pom.xml 01-person-service/pom.xml
COPY 01-person-service/src     01-person-service/src

# Optional: Pre-fetch dependencies to leverage Docker caching
RUN ./mvnw dependency:go-offline

# Build person-service and its dependency (00-common)
RUN ./mvnw clean package -pl 01-person-service -am -DskipTests

# -------- Stage 2: Build the runtime image --------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
VOLUME /tmp
ENV SPRING_PROFILES_ACTIVE=prod

# Copy the built JAR file from the build stage
COPY --from=builder /build/01-person-service/target/person-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
