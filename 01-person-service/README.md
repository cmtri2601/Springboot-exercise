# Person Service

The Person Service is a Spring Boot microservice responsible for managing person data, including personal information and tax-related data. It is a core component of the microservices architecture and interacts with other services via REST and Kafka.

## Features

- CRUD operations for person entities
- Stores and manages tax-related data for each person
- Exposes RESTful API endpoints
- Integrates with PostgreSQL for data persistence
- Uses Liquibase for database migrations
- Publishes and consumes messages via Kafka

## Project Structure

```
person/
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

Build the Person Service using Maven:

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

## API Endpoints

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

## Database Schema

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

