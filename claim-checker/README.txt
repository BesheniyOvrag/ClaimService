# Claim Checker — Insurance Claim Pre-Checker

**Short description:**
Claim Checker is a proof-of-concept web application that accepts insurance claims via a Vaadin UI, classifies them with an LLM (via a configurable API endpoint), stores the result in PostgreSQL (JPA/Hibernate) and exposes a small REST surface.

---

# Table of contents

1. Overview & goals
2. Architecture & components (what each part does)
3. How AI integration works (and how to adapt to API-key models)
4. Running locally (quickstart + Docker Compose)
5. Configuration (env vars / properties)
6. API & UI usage examples (sample requests, UI behaviour)
7. Important bugs encountered & how they were fixed (deep analysis)

---

# 1) Overview & goals

This project demonstrates an end-to-end flow:

* UI (Vaadin) where user submits claim details (policy type, name, surname, email, date, description).
* Business service (`ClaimCheckerService`) calls `AiService` to classify the claim text.
* The returned `decision` is stored in a `Claim` JPA entity alongside `booleanDecision` derived from the text.
* All saved claims are visible in the Vaadin `Grid` and available through a small REST API (`/claims`).

Primary goals:

* Fast prototyping of human-in-the-loop pre-checker for insurance claims.
* Clean separation: UI → Service → AiService → Repository.
* Make the AI backend pluggable (any model behind an HTTP API).

---

# 2) Architecture & components

**High-level flow**

```
Vaadin UI (ClaimView)  -> ClaimCheckerService -> AiService (HTTP to model) -> ClaimRepository (JPA) -> PostgreSQL
                         ^                                                                |
                         |----------------------------------------------------------------|
```

**Main modules / files**

* `com.example.claim_checker.web.ClaimView` — Vaadin form + validation + `Grid<Claim>` to display DB content.
* `com.example.claim_checker.service.ClaimCheckerService` — orchestrates validation, calls `AiService`, maps DTO → `Claim` entity, saves with `ClaimRepository`.
* `com.example.claim_checker.service.AiService` — does HTTP calls to the model endpoint (OkHttp); builds prompt and parses model response.
* `com.example.claim_checker.entity.Claim` — JPA entity (fields: id, policyType(enum), name, surname, email, claimDate(LocalDate), description, decision, booleanDecision).
* `com.example.claim_checker.repository.ClaimRepository` — Spring Data JPA.
* `com.example.claim_checker.controller.ClaimController` — simple REST API (POST /claims, GET /claims).
* `com.example.claim_checker.model.ClaimRequest` / `ClaimResponse` — DTOs.
* `application.properties` — database and AI endpoint configuration.
* `db/migration` (recommended) — SQL migrations (Flyway suggested), currently project includes a create table SQL.

---

# 3) AI integration (how it currently works and how to use API-key models)

**Current code:** `AiService.classifyClaim(ClaimRequest)` uses OkHttp and `ollama.api.url` + `ollama.model` from properties. It builds a prompt instructing the model to respond with one of two labels and justification, then parses newline-separated JSON lines to collect `response` fields.

**How to adapt to API-keyed models (generic advice)**:

* Put API key in environment variable; **never** commit it.
* Add property: `ollama.api.key` (or `MODEL_API_KEY`) and send a header:

  ```java
  Request.Builder rb = new Request.Builder().url(apiUrl).post(body);
  if(apiKey != null && !apiKey.isBlank()) {
      rb.header("Authorization", "Bearer " + apiKey);
  }
  Request request = rb.build();
  ```
* If the AI provider returns streaming SSE or line-based JSON, reuse the existing line-by-line parsing pattern — but make parsing fault tolerant.

**Improved AiService pattern (recommended)**:

* Use timeouts and a retry/backoff strategy.
* Limit prompt length and sanitize user description (avoid injection).
* Provide configurable headers (API-Key, Accept).
* Provide a test/mock mode for local development (AiServiceMock).

**Example: send API key if present**

```java
@Value("${model.api.key:}")
private String apiKey;

Request.Builder builder = new Request.Builder()
        .url(apiUrl)
        .post(RequestBody.create(requestBody, MediaType.get("application/json")));
if (!apiKey.isBlank()) {
    builder.addHeader("Authorization", "Bearer " + apiKey);
}
Request request = builder.build();
```

---

# 4) Running locally — Quickstart (recommended)

**Prerequisites**

* Java 17
* Maven
* Docker (optional, for Postgres)
* PostgreSQL running at `jdbc:postgresql://localhost:5432/Insurance` (or use Docker Compose below)
* Model endpoint reachable (Ollama or other model endpoint). If using an external API, set `ollama.api.url` to provider endpoint and set `ollama.model` accordingly and `MODEL_API_KEY` if required.

**Docker Compose (Postgres) — sample**

```yaml
# docker-compose.yml
version: "3.8"
services:
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: Insurance
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
volumes:
  pgdata:
```

`docker compose up -d`

**Database migration / init**
Two choices:

* Use the provided SQL in the repo to create `claims` table (or let Hibernate `ddl-auto=update` create it).
* **Recommended:** Add Flyway and create `src/main/resources/db/migration/V1__init.sql` with the schema. Example SQL (consistent with Java enum uppercase):

```sql
CREATE TABLE IF NOT EXISTS claims (
  id BIGSERIAL PRIMARY KEY,
  policy_type VARCHAR(20) NOT NULL CHECK (policy_type IN ('PREMIUM','STANDARD','ECONOMY')),
  name VARCHAR(50) NOT NULL,
  surname VARCHAR(50) NOT NULL,
  email VARCHAR(50) NOT NULL,
  claim_date DATE NOT NULL,
  description TEXT NOT NULL,
  decision TEXT,
  booleanDecision BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP DEFAULT NOW()
);
```

**Run application**

1. Configure env/properties (see next section).
2. Build & run:

```bash
mvn clean package
mvn spring-boot:run
# or
java -jar target/claim-checker-0.0.1-SNAPSHOT.jar
```

---

# 5) Configuration (application.properties / recommended env names)

**Properties used in repo**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/Insurance
spring.datasource.username=postgres
spring.datasource.password=123

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update

ollama.api.url=http://localhost:11434/api/generate
ollama.model=gemma3:4b

server.port=8081
```

---

# 6) API & UI usage examples

**REST API**

* `POST /claims` — body: `ClaimRequest` JSON

```json
{
  "policyType": "Standard",
  "name": "John",
  "surname": "Doe",
  "email": "john.doe@example.com",
  "date": "2025-08-07",
  "description": "Patient with non-specific symptoms..."
}
```

Response:

```json
{ "decision": "Likely to deny\n\nJustification: ..." }
```

* `GET /claims` — returns list of saved `Claim` entities.

**Vaadin UI**

* Route: `http://localhost:8081/` (based on `@Route("")`).
* Form fields: Policy Type (Select), Name, Surname, Email, Date (DatePicker), Description (TextArea), Send button.
* Grid shows: ID, Policy Type, Name, Surname, Email, Date, Description, Decision, Status (booleanDecision).
* Validation: all fields required, date must not be future.

---

# 7 Important bugs encountered & how they were fixed (deep analysis)

1. **Postgres `CHECK` / enum constraint failure (SQLState 23514)**

   * Cause: mismatch between UI strings (e.g., `"Standard"`) and DB constraint values (`'STANDARD'` or `'standard'`).
   * Fix: normalize input and map to Java enum: `PolicyType.valueOf(request.getPolicyType().toUpperCase())`. Align DB migration to use uppercase `'PREMIUM','STANDARD','ECONOMY'` or use `VARCHAR`+`CHECK`. Validate in UI.

2. **Date validation**

   * Cause: earlier code validated `datePicker.toString()` which is wrong.
   * Fix: validate `LocalDate date = datePicker.getValue()` with `date != null` and `!date.isAfter(LocalDate.now())`.

3. **AI response parsing**

   * Cause: model returns streaming JSON or multi-line text; initial code concatenated lines and sometimes left markup (asterisks).
   * Fix: parse line-by-line JSON; extract `response` fields; sanitize `decision = decision.replace("*", "").trim();`. Consider more robust parsing (SSE clients or official SDK).

4. **Data privacy**

   * Observed storing PII (name, surname, email). Add privacy considerations (mask logs, validate email, retention policy).

---
