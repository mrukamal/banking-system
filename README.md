# Banking System

Simple Spring Boot microservice for managing bank accounts backed by PostgreSQL.

This repository contains a REST API exposing basic CRUD operations for customers and accounts. It uses Spring Boot, Spring Data JPA, and PostgreSQL.

## Overview

- Language: Java 21
- Frameworks: Spring Boot 3.4.1 (Web), Spring Data JPA, Lombok
- Build tool / Package manager: Maven 3.9+ (with Maven Wrapper)
- Entry point: `com.bankingsystem.BankingSystemApplication`
- Persistence: PostgreSQL (tables: `customers`, `accounts`)
- REST base paths: `/api/customers`, `/api/accounts`

### Exposed Endpoints

#### Customers API

- `GET /api/customers` — list all active customers (with their accounts)
- `GET /api/customers/{id}` — get customer by id (with their accounts)
- `POST /api/customers` — create customer
- `PUT /api/customers/{id}` — update customer by id
- `DELETE /api/customers/{id}` — soft-delete customer by id

Sample customer request:
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com"
}
```

Sample customer response:
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "active": true,
  "accounts": [
    {
      "id": 1,
      "accountHolderName": "John Doe",
      "accountType": "SAVINGS",
      "balance": 1000.0,
      "active": true,
      "createdAt": "2025-12-19T10:00:00",
      "lastModifiedAt": "2025-12-19T10:00:00"
    }
  ],
  "createdAt": "2025-12-19T10:00:00",
  "lastModifiedAt": "2025-12-19T10:00:00"
}
```

#### Accounts API

- `GET /api/accounts` — list all active accounts
- `GET /api/accounts/{id}` — get account by id
- `POST /api/accounts` — create account
- `PUT /api/accounts/{id}` — update account by id
- `DELETE /api/accounts/{id}` — soft-delete account by id

Sample account request:
```json
{
  "accountHolderName": "John Doe",
  "accountType": "SAVINGS",
  "balance": 1000.00
}
```

Sample account response:
```json
{
  "id": 1,
  "accountHolderName": "John Doe",
  "accountType": "SAVINGS",
  "balance": 1000.0,
  "active": true,
  "createdAt": "2025-12-19T10:00:00",
  "lastModifiedAt": "2025-12-19T10:00:00"
}
```

## Requirements

- JDK 23
- Maven 3.9+ (or use the provided Maven Wrapper `mvnw`/`mvnw.cmd`)
- Docker and Docker Compose

## Configuration

Configuration files:

- `src/main/resources/application.yaml` — base config
- `src/main/resources/application-local.yaml` — local profile overrides

Important properties (local profile):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bankingsystem
    username: banking_system
    password: wgZ8W&t6
server:
  port: 8089
```

## Running the app

### 1) Start dependencies with Docker Compose

This project provides a `docker-compose.yaml` to start PostgreSQL and Adminer. Run it from the project root:

```powershell
docker-compose up -d
```

- **PostgreSQL**: `localhost:5432`
- **Adminer (DB Management)**: `http://localhost:8080` (use `db:5432` as server in Adminer login)

### 2) Run the service (development)

Using Maven Wrapper:

Windows:
```powershell
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Unix:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The app will start on `http://localhost:8089` when using the `local` profile.

## Testing

Run unit and integration tests:

```bash
mvn test
```

## Project structure

```
.
├── docker-compose.yaml
├── pom.xml
├── src
│   ├── main
│   │   ├── java/com/bankingsystem
│   │   │   ├── controller/      # REST endpoints
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── repository/      # Spring Data Repositories
│   │   │   └── service/         # Business logic
│   │   └── resources
│   │       ├── application.yaml
│   │       └── application-local.yaml
│   └── test/java/com/bankingsystem
└── README.md
```

## Changelog

- 2025-12-19: Updated README with PostgreSQL instructions, Adminer info, and detailed API specs.