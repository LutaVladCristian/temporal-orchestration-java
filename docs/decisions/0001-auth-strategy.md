# 0001: No Auth Yet, Treat the Service as Local-Only

- Status: accepted
- Date: 2026-05-15

## Context

The current backend exposes upload and read endpoints without any authentication or authorization. The repository appears to be in an early ingestion-focused stage, and the code does not yet include Spring Security, user accounts, or tenant scoping.

## Decision

Do not introduce partial or ad hoc authentication yet. Document the service as local-development-only until a real identity and authorization model is added.

## Rationale

- The current data model has no concept of user ownership.
- Adding auth without row ownership would protect the edge but not solve data isolation.
- The application is still missing larger functional pieces, including tax-calculation logic and the AI layer described in repo notes.

## Consequences

### Positive

- keeps the current code simple while the ingestion workflow is still moving
- avoids a misleading sense of security from a thin auth layer

### Negative

- the service is unsafe for shared or public deployment
- contributors must assume all endpoints are effectively public inside the reachable network

## Revisit triggers

Revisit this decision when any of the following happens:

- a browser client is restored or reintroduced
- the API is exposed beyond localhost
- statement ownership becomes part of the domain model
- AI-generated reports are added on top of persisted financial data
