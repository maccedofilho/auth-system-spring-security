# Auth System — Production-Ready Authentication API

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?style=for-the-badge&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)

A production-oriented authentication and authorization system built with **Spring Boot 3** and **Spring Security 6**. Designed with a security-first mindset, this API focuses on token lifecycle control, session integrity, abuse prevention, and operational robustness.

## Key Features

### Advanced Security
* **Token Lifecycle:** Short-lived JWT access tokens (15 minutes) with JTI-based revocation (blacklist).
* **Refresh Token Rotation:** Prevents token theft replay attacks; secure storage using SHA-256 hashing.
* **Account Protection:** Automatic account lockout after repeated failed login attempts.
* **Password Policy:** Enforced strong passwords (12+ chars, complexity rules) and secure reset flows.

### Operational Robustness
* **Session Tracking:** detailed tracking (IP, device, User-Agent) with selective session revocation.
* **Rate Limiting:** Endpoint-specific limits to prevent abuse.
* **Hardened Headers:** Pre-configured CSP, HSTS, and frame protection.
* **RBAC:** Granular Role-Based Access Control (`ROLE_USER`, `ROLE_ADMIN`).

## Tech Stack

* **Core:** Java 21, Spring Boot 3.x
* **Security:** Spring Security 6.x, JWT (JJC/Nimbus)
* **Data:** Spring Data JPA, Hibernate 6.x, PostgreSQL 16
* **Build:** Maven

## Running Locally

### 1. Start Database
Run the following command to spin up the PostgreSQL container:

```bash
docker run -d \
  --name postgres-auth \
  -e POSTGRES_USER=macedo \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=authsystem \
  -p 5432:5432 \
  postgres:16-alpine

2. Run Application
Once the database is ready, start the application:

Bash
./mvnw spring-boot:run

API Documentation
Interactive Swagger UI documentation will be available at:

http://localhost:8080/swagger-ui.html

