# 0002: Use PostgreSQL as the System Database

- Status: accepted
- Date: 2026-05-15

## Context

The repository uses:

- PostgreSQL JDBC driver
- PostgreSQL dialect
- PostgreSQL container image in compose
- hand-written SQL migration scripts

The application stores normalized transaction rows that fit a relational model and are later intended to support downstream tax analysis.

## Decision

Use PostgreSQL 17 as the primary application database for local and containerized runtime.

## Rationale

- the current code and configuration now target PostgreSQL directly
- the domain is strongly relational
- JPA and Temporal activities both work naturally against a transactional SQL database
- PostgreSQL has lower local setup friction and broader contributor familiarity

## Consequences

### Positive

- straightforward fit for normalized financial records
- mature JDBC and Spring support
- local parity via Docker
- simpler default local bootstrap through `POSTGRES_DB`

### Negative

- manual schema setup is still required in this repository
- PostgreSQL-specific setup still ties local workflows to Docker or an external database
- type choices in the current schema, such as string storage for some monetary fields, still need cleanup

## Follow-up work

- automate schema creation and migrations
- clean up remaining Spring Batch-oriented configuration that no longer matches the Temporal import flow
- tighten numeric typing for monetary columns
