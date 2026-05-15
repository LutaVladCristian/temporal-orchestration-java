# trading-tax-calculator-ai

This repository currently contains a Spring Boot service that ingests trading statement CSV files, parses them with Spring Batch, and stores normalized rows in Microsoft SQL Server.

The broader AI-assisted tax calculator described in older repo notes is not implemented in the current backend code. In this checkout, the previously referenced `frontend/` app is also deleted from the working tree, so the runnable surface is the backend and database setup.

## Documentation

- [Architecture](docs/architecture.md)
- [API](docs/api.md)
- [Auth](docs/auth.md)
- [Deployment](docs/deployment.md)
- [Local development runbook](docs/runbooks/local-dev.md)
- [Production debugging runbook](docs/runbooks/production-debugging.md)
- [ADR 0001: auth strategy](docs/decisions/0001-auth-strategy.md)
- [ADR 0002: database choice](docs/decisions/0002-database-choice.md)
- [Contributor guide](CONTRIBUTING.md)

## Quick start

1. Start SQL Server from `spring-server/`:
   ```bash
   docker compose up -d
   ```
2. Create `SERVER_DB`.
3. Apply SQL scripts from `database-setup/`.
4. Start the backend:
   ```bash
   cd spring-server
   mvn spring-boot:run
   ```

Backend base URL:

```text
http://localhost:8080/spring-boot-api
```

## Repository layout

```text
spring-server/   Spring Boot backend
database-setup/  SQL scripts
docs/            project documentation
```
