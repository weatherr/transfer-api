# Fibank Transfer API

A robust, concurrent, double-entry bookkeeping microservice for processing inter-account bank transfers and scheduling standing orders. Built for Fibank's Senior Backend Developer assignment.

## Tech Stack
Java 25
Spring Boot 4.1.0
Maven 3
H2 Database (In-Memory)
Liquibase (Schema Versioning & Seed Data)
ShedLock (Distributed Cron Job Locking)
Log4j2 + LMAX Disruptor (Asynchronous Logging)

## Architecture Decisions

1. **Double-Entry Bookkeeping:** A strict append-only ledger is used. Every transfer guarantees atomic persistence of a `DEBIT` and a `CREDIT` record.
2. **Concurrency & Locking:** To prevent race conditions during high-volume transfers, `@Version` optimistic locking is implemented on the `Account` entity.
3. **Idempotency:** A 24-hour in-memory cache intercepts duplicate `X-Idempotency-Key` headers at the controller level, preventing identical transfers from executing twice while returning the original response.
4. **Distributed Standing Orders:** Scheduled tasks are secured using **ShedLock**. This guarantees that in a multi-instance deployment, only one instance executes the cron schedule at a time, preventing duplicate background transfers.
5. **Dynamic Ledger Querying:** The audit endpoint utilizes `JpaSpecificationExecutor` to dynamically construct filtered queries without hardcoding dozens of repository combinations.

## Build & Run Instructions

### Option 1: Docker
The application is fully containerized. To run it immediately:
```bash
docker compose up --build
```

### Option 2: Local Maven Build
To run tests and boot the application locally:
mvn clean test
mvn spring-boot:run

### Security
All endpoints are secured via a custom API key header.
Header: X-FIB-AUTH
Value: default-dev-key (Configured in application.yml)

### The Postman Collection
A complete Postman collection is included in the root of this repository: Fibank_Transfer_API.postman_collection.json.