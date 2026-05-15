# 0002: Use Microsoft SQL Server as the System Database

- Status: accepted
- Date: 2026-05-15

## Context

The repository uses:

- SQL Server JDBC driver
- SQL Server dialect
- SQL Server container image in compose
- hand-written SQL migration scripts

The application stores normalized transaction rows that fit a relational model and are later intended to support downstream tax analysis.

## Decision

Use Microsoft SQL Server 2022 as the primary application database for local and containerized runtime.

## Rationale

- the current code and configuration already target SQL Server directly
- the domain is strongly relational
- JPA and Spring Batch both work naturally against a transactional SQL database
- the repository already contains SQL Server-specific setup and operational assumptions

## Consequences

### Positive

- straightforward fit for normalized financial records
- mature JDBC and Spring support
- local parity via Docker

### Negative

- manual schema setup is still required in this repository
- SQL Server-specific setup raises contributor friction compared with lighter embedded options
- type choices in the current schema, such as string storage for some monetary fields, still need cleanup

## Follow-up work

- automate schema creation and migrations
- add Spring Batch metadata schema management
- tighten numeric typing for monetary columns
