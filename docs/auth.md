# Auth

## Current state

There is no authentication or authorization in the current codebase.

The backend does not use Spring Security, does not issue tokens, and does not protect any endpoint. Any client that can reach the service can:

- upload CSV files
- query batch job status
- read all persisted transaction data

## CORS behavior

`WebConfig` allows browser requests from:

```text
http://localhost:5173
```

Allowed methods:

- `GET`
- `POST`
- `PUT`
- `DELETE`
- `OPTIONS`

CORS is not an access control mechanism. It only affects browser behavior.

## Risk profile

For anything beyond local development, the current posture is unsafe:

- uploaded statements may contain sensitive financial data
- all persisted rows are globally readable
- there is no user isolation
- there is no audit trail

## Recommended direction

If the project moves beyond local use, add authentication before adding more data exposure or AI features.

Suggested order:

1. Add Spring Security.
2. Require authenticated access to all `/spring-boot-api/**` routes.
3. Introduce user or tenant ownership on uploaded statements and derived rows.
4. Scope reads by statement id and owner.
5. Add audit logging for uploads and downstream tax calculations.

## Candidate strategy

For a browser-based client and an API backend, the most practical default would be:

- OIDC/OAuth 2.0 with an external identity provider
- backend validation of JWT bearer tokens
- authorization at the service layer based on user ownership

## What this doc means for contributors

Until auth exists, treat the application as a local-development-only system.
