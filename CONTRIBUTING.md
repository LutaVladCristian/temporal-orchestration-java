# Contributing

## Scope

This guide reflects the repository as it exists in the current checkout: a Spring Boot backend plus SQL Server setup scripts.

## Prerequisites

- Java 21
- Maven
- Docker
- SQL Server access for local setup

## Setup

Follow the local runbook:

- [docs/runbooks/local-dev.md](docs/runbooks/local-dev.md)

## Development expectations

### Keep changes scoped

- prefer incremental changes over broad refactors
- match existing Spring Boot, JPA, and Spring Batch patterns
- leave unrelated worktree changes alone

### Database changes

- add or update SQL scripts under `database-setup/`
- keep entity mappings and SQL schema in sync
- document schema-impacting decisions in `docs/decisions/` when they affect architecture

### API changes

When changing endpoints or payloads:

- update `docs/api.md`
- update `docs/architecture.md` if the flow changes materially

### Operational changes

When changing runtime configuration, container setup, or startup steps:

- update `docs/deployment.md`
- update affected runbooks in `docs/runbooks/`

## Testing

Current automated coverage is minimal. At a minimum, run:

```bash
cd spring-server
mvn clean test
```

For changes affecting packaging or container runtime, also validate:

```bash
mvn clean package -DskipTests
docker compose up -d --build
```

## Documentation standard

Treat the docs as part of the codebase. If your change modifies behavior, setup, operational expectations, or architecture, update the corresponding document in the same change.
