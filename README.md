# hcx-ss-sftp-send

Sends SureScripts SFTP request files for panels in "Request Created" status.

## Process Flow

`POST /api/v1/sftp/send` with `{"customerId": <id>}`:
1. Queries `panel` table for all panels in "Request Created" status for the customer
2. Downloads each panel's file from S3 (`{bucket}/{request_processed_location}/{sent_request_filename}`)
3. SFTPs the file to the SureScripts SFTP server (single connection enforced regardless of concurrency)
4. Moves the S3 file to `{bucket}/{request_sent_location}/{sent_request_filename}`
5. Updates `panel.status` to "Request Sent"

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/api/v1/sftp/send` | Trigger file send for a customer |
| `GET`  | `/actuator/health` | Health check |
| `PUT`  | `/api/v1/admin/log-level` | Change log level at runtime |
| `GET`  | `/api/v1/admin/log-level?loggerName=…` | Get current log level |

### cURL Examples

**Trigger file send for a customer**
```bash
curl -s -X POST http://localhost:8093/api/v1/sftp/send \
  -H "Content-Type: application/json" \
  -d '{"customerId": 123}'
```

**Health check**
```bash
curl -s http://localhost:8093/actuator/health | jq
```

**Change log level at runtime**
```bash
curl -s -X PUT http://localhost:8093/api/v1/admin/log-level \
  -H "Content-Type: application/json" \
  -d '{"loggerName": "com.healthconnexx", "level": "DEBUG"}'
```

**Get current log level**
```bash
curl -s "http://localhost:8093/api/v1/admin/log-level?loggerName=com.healthconnexx" | jq
```

## Deployment

### Build (once, any environment)

```bash
./mvnw -DskipTests package
docker build -t hcx-ss-sftp-send:latest .
```

### Run Locally (LocalStack + external Postgres)

```bash
cp .env.local.example .env.local
# Edit .env.local — update SPRING_DATASOURCE_URL, USERNAME, PASSWORD for your local DB
HCX_ENV_FILE=.env.local docker-compose up
```

App will be available at `http://localhost:8093`.

### Run QA

```bash
cp .env.qa.example .env.qa
# Populate with QA secrets (or inject via CI/CD)
HCX_ENV_FILE=.env.qa docker-compose up -d
```

### Run Production

```bash
# Credentials should be injected via CI/CD secret management (ECS task def, k8s secrets, etc.)
HCX_ENV_FILE=.env.prod docker-compose up -d
```

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `SPRING_PROFILES_ACTIVE` | Yes | `local`, `qa`, or `prod` |
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC URL with `?currentSchema=healthdata` |
| `SPRING_DATASOURCE_USERNAME` | Yes | DB username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | DB password |
| `AWS_REGION` | Yes | AWS region (e.g., `us-east-1`) |
| `AWS_ACCESS_KEY_ID` | Local only | LocalStack dummy: `test` |
| `AWS_SECRET_ACCESS_KEY` | Local only | LocalStack dummy: `test` |
| `AWS_S3_ENDPOINT` | Local only | LocalStack URL: `http://localstack:4566` |
| `APP_HOST_PORT` | No | Host port mapping (default `8093`) |
| `SURESCRIPTS_SFTP_HOST` | Yes | SureScripts SFTP hostname |
| `SURESCRIPTS_SFTP_USERNAME` | Yes | SureScripts SFTP username |
| `SURESCRIPTS_SFTP_PASSWORD` | Yes | SureScripts SFTP password |
| `SURESCRIPTS_SFTP_REMOTE_DIRECTORY` | Yes | Remote directory path on SFTP server |

## Change Log Level at Runtime

```bash
# Raise com.healthconnexx to DEBUG
PUT /api/v1/admin/log-level
{"loggerName": "com.healthconnexx", "level": "DEBUG"}

# Or via Actuator
POST /actuator/loggers/com.healthconnexx
{"configuredLevel": "DEBUG"}

# Reset to default
DELETE /actuator/loggers/com.healthconnexx
```

