# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AI-powered trading tax calculator that uses an MCP (Model Context Protocol) server with RAG (Retrieval-Augmented Generation) to analyze trading transactions from CSV exports and calculate tax liabilities. The system parses brokerage CSV statements, persists transactions via JPA, and uses a Claude AI agent to apply jurisdiction-specific tax rules.

## Database (Docker)

The database is **Microsoft SQL Server 2022** running in Docker. Start it before running the application:

```bash
cd BackEndServer
docker compose up -d
```

To stop it: `docker compose down`

On first run, the database `SERVER_DB` must be created manually. Connect to `master` with the credentials below and run:

```sql
CREATE DATABASE SERVER_DB;
```

**Connection details** (use the SQL Server extension in VS Code):

| Field | Value |
|---|---|
| Server name | `localhost` |
| Authentication | SQL Login |
| User name | `sa` |
| Password | `MyStrongP@ssword1` |
| Database name | `SERVER_DB` |

### Database schema

Migration scripts live in `database-setup/`. Run them in order against `SERVER_DB`.

## Backend Commands

All commands run from the `BackEndServer/` directory using system Maven (`mvn`).

```bash
# Start the server
cd BackEndServer
mvn spring-boot:run

# Build (skip tests)
mvn clean package -DskipTests

# Run all tests
mvn clean test

# Run a single test class
mvn test -Dtest=BackEndServerApplicationTests
```

## Tech Stack

- **Java 21** with **Spring Boot 3.5**
- **Spring Batch** — chunk-oriented processing of CSV trading statements
- **Spring Data JPA / Hibernate** — persistence layer
- **Lombok** — boilerplate reduction (`@Data`, `@Builder`, etc.)
- **Microsoft SQL Server 2022** (Docker) — persistence store
- **mssql-jdbc** — SQL Server JDBC driver

## Architecture

```
CSV Upload → UploadCsvController
                ↓
          UploadCsvService  (business logic + batch orchestration)
                ↓
          Spring Data JPA → Database
```

Key packages under `com.example.BackEndServer`:
- `server/` — CSV upload flow: controller, service, and DTOs (`UploadCsvDto`, `UploadCsvInputDto`)

The sample trading statement at `src/main/resources/2025_tax_year_statement.csv` shows the expected CSV schema: sales (date acquired/sold, symbol, quantity, cost basis, proceeds, P&L) and dividends (date, symbol, gross, withholding, net, currency). Multi-currency (USD/EUR/RON) and multi-asset-class data is expected.

## Configuration

`src/main/resources/application.properties` currently only sets `spring.application.name`. Before running, you will need to add:

- `spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=SERVER_DB;encrypt=false`
- `spring.datasource.username=sa`
- `spring.datasource.password=MyStrongP@ssword1`
- `spring.jpa.hibernate.ddl-auto`
- `spring.batch.job.enabled` / `spring.batch.jdbc.initialize-schema`