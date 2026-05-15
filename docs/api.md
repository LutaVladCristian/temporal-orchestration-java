# API

## Base URL

Local default:

```text
http://localhost:8080/spring-boot-api
```

## Content types

- Upload endpoint: `multipart/form-data`
- Read endpoints: `application/json`

## Endpoints

### `POST /upload-csv`

Uploads a trading statement CSV and starts a Spring Batch job.

#### Form fields

- `name`: string
- `file`: CSV file

#### Example

```bash
curl -X POST http://localhost:8080/spring-boot-api/upload-csv \
  -F "name=2025 tax year" \
  -F "file=@spring-server/src/main/resources/2025_tax_year_statement.csv"
```

#### Success response

```json
{
  "jobExecutionId": 123
}
```

#### Notes

- The backend reads the uploaded file fully into memory before creating the batch job.
- The `name` parameter is currently only included in job parameters. It is not persisted in the database.

### `GET /job-status/{executionId}`

Returns the Spring Batch execution status for a previously started job.

#### Example

```bash
curl http://localhost:8080/spring-boot-api/job-status/123
```

#### Success response

```json
{
  "status": "COMPLETED"
}
```

#### Possible statuses

- `STARTING`
- `STARTED`
- `COMPLETED`
- `FAILED`
- `STOPPED`

#### Error response

- `404 Not Found` if the execution id does not exist

### `GET /income-from-sells`

Returns all persisted sale rows.

#### Example response

```json
[
  {
    "id": 1,
    "dateAcquired": "2025-01-24",
    "dateSold": "2025-01-27",
    "symbol": "DAL",
    "securityName": "Delta Air Lines",
    "isin": "US2473617023",
    "country": "US",
    "quantity": 7.78598899,
    "costBasis": 524.62,
    "grossProceeds": 529.48,
    "grossPnl": 4.86,
    "currency": "USD"
  }
]
```

### `GET /other-income-fees`

Returns all persisted non-sale income rows.

#### Example response

```json
[
  {
    "id": 1,
    "date": "2025-04-03",
    "symbol": "NVDA",
    "securityName": "NVIDIA dividend",
    "isin": "US67066G1040",
    "country": "US",
    "grossAmount": 0.45,
    "withholdingTax": "0.04 RON",
    "netAmount": "0.41 RON",
    "currency": "RON"
  }
]
```

## Error behavior

The controller does not define a custom exception model. Unhandled failures from upload or batch startup currently surface through Spring Boot's default error handling.

Typical failure classes:

- invalid or unreadable upload
- batch job launch failure
- database connection failure
- CSV parsing failure

## API limitations

- No auth
- No pagination
- No filtering
- No statement-level scoping; `GET` endpoints return all rows in their tables
- No delete or reprocessing workflow
- No stable contract for validation errors
