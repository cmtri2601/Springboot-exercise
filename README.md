# Springboot Exercise Project

This project demonstrates a microservices architecture using Spring Boot, Kafka, and Kubernetes. It consists of two main services that work together to manage person data and perform tax-related operations.

## Project Structure

```
project/
├── docker-compose.yaml           # Main docker-compose file
├── cron/                         # Cron service for scheduled tasks
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/                      # Source code
├── person/                       # Person service for managing person data
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/                      # Source code
├── k8s-1-node/                   # Kubernetes configuration for single node
│   ├── deploy.sh
│   ├── delete.sh
│   ├── replace.sh
│   └── forward.sh
│   └── development               # Kubernetes development files
├── k8s-multiple-nodes/           # Kubernetes configuration for multiple nodes
│   └── deployment.yaml
└── kafka-dev/                    # Kafka development environment
    └── docker-compose.yaml
```

## Technologies

- Java 21
- Spring Boot 3.5.0
- Spring Data JPA
- PostgreSQL
- Liquibase (database migrations)
- Kafka (messaging)
- Docker & Kubernetes
- Maven

## Services

### Person Service

The Person service manages person data including personal information and tax-related data.

#### Database Schema

```sql
CREATE TABLE persons (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    tax_id VARCHAR(50) NOT NULL UNIQUE,
    tax_debt NUMERIC(15, 2) NOT NULL
);
```

#### Endpoints

- `GET /api/v1/person/search?prefix={}&age={}` - Search person by prefix and age
- `GET /api/v1/persons` - Get all persons
- `GET /api/v1/persons/{id}` - Get person by ID
- `GET /api/v1/persons/tax-id/{id}` - Get person by TaxID
- `POST /api/v1/persons` - Create new person
- `PATCH /api/v1/persons/{id}` - Update person
- `DELETE /api/v1/persons/{id}` - Delete person

- `POST /api/v1/tax-calculation` - Calculate tax for one message
- `POST /api/v1/tax-calculation/batch` - Calculate tax for a batch of messages
- `POST /api/v1/tax-calculation/manual` - Produce manual tax calculation messages
- `GET /api/v1/tax-calculation/manual?count={}` - Consume manual tax calculation messages

### Cron Service

The Cron service handles scheduled tasks and processes tax calculation messages from Kafka.

#### Features

- Consumes tax calculation messages from Kafka
- Performs scheduled operations
- Exposes health and metrics endpoints via Spring Actuator

## Building and Running

### Building the Services

#### Person Service

```bash
cd person
./mvnw clean package
```

#### Cron Service

```bash
cd cron
./mvnw clean package
```

### Running with Docker Compose

```bash
docker-compose up -d
```

### Building Docker Images

Both services use the `jib-maven-plugin` to build Docker images:

```bash
# Build person service image
cd person
./mvnw clean compile jib:build

# Build cron service image
cd cron
./mvnw clean compile jib:build
```

### Deploying to Kubernetes

#### Single Node

```bash
cd k8s-1-node
./deploy.sh
```

#### Multiple Nodes

```bash
cd k8s-multiple-nodes
kubectl apply -f deployment.yaml
```

## Development Setup

1. Start the Kafka development environment:
   ```bash
   cd kafka-dev
   docker-compose up -d
   ```

2. Run the services locally with Spring profiles:
   ```bash
   # Person service
   ./mvnw spring-boot:run -Dspring.profiles.active=local

   # Cron service
   ./mvnw spring-boot:run -Dspring.profiles.active=local
   ```

