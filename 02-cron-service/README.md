# Cron Service

The Cron Service is a Spring Boot microservice responsible for handling scheduled tasks and processing tax calculation messages from Kafka. It is designed to work as part of a microservices architecture alongside the Person Service.

## Features

- Consumes tax calculation messages from Kafka topics
- Performs scheduled operations (e.g., periodic tax calculations)
- Exposes health and metrics endpoints via Spring Actuator
- Integrates with PostgreSQL for data persistence

## Project Structure

```
cron/
├── Dockerfile
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/           # Java source code
│   │   └── resources/      # Configuration files
│   └── test/
└── ...
```

## Configuration

Application configuration files are located in `src/main/resources/`:
- `application.yaml` (default)
- `application-dev.yaml` (development)
- `application-prod.yaml` (production)

## Building

Build the Cron Service using Maven:

```bash
./mvnw clean package
```

## Running

### Locally

```bash
./mvnw spring-boot:run -Dspring.profiles.active=local
```

### With Docker

Build the Docker image using Jib:

```bash
./mvnw clean compile jib:build
```

Or use Docker Compose from the project root:

```bash
docker-compose up -d
```

## Endpoints

- `/actuator/health` - Health check
- `/actuator/metrics` - Metrics



