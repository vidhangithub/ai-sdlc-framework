---
name: generate-lld
version: 1.0.0
phase: "02 — Design Documents"
description: >
  Generates a complete Low-Level Design (LLD) document from an approved HLD and
  locked LRS. Use this skill whenever a user wants detailed technical design
  including API contracts, database schema, class diagrams, sequence diagrams,
  error handling strategy, or messaging design. Trigger when the user says
  "generate LLD", "create detailed design", "design the API", "create database
  schema", "generate sequence diagrams", "design the service internals", or
  "write the low-level design". This skill must always follow HLD approval —
  never generate LLD from raw requirements directly.
inputs:
  - name: hld
    description: Full content of the approved High-Level Design document
    required: true
  - name: lrs
    description: Full content of the Locked Requirement Specification
    required: true
  - name: service_name
    description: Name of the Spring Boot service being designed
    required: true
  - name: package_base
    description: "Java package base, e.g. com.company.payments"
    required: true
output: Low-Level Design (LLD) document with API specs, DB schema, class and sequence diagrams
---

# Skill: Generate Low-Level Design (LLD)

## Purpose

Produce a detailed, implementation-ready LLD that engineers can build from directly. Every API contract, database table, class, and integration pattern must be specified precisely enough that AI code generation (skill 05) can produce correct scaffolding from it.

---

## Pre-conditions

- HLD must be Status: `APPROVED` — if not, warn and proceed as provisional
- LRS must be Status: `LOCKED`
- `service_name` and `package_base` must be provided

---

## Step-by-Step Instructions

### Step 1 — Derive Service Responsibilities

From the HLD component diagram, identify this service's exact responsibilities:
- What it owns (its bounded context)
- What it exposes (APIs)
- What it consumes (APIs, MQ topics, DB)
- What events it publishes

State these as a clear bullet list before designing anything.

### Step 2 — Define Package Structure

Output the complete Java package structure following company conventions:

```
{package_base}/
├── config/              # Spring @Configuration classes
├── controller/          # @RestController classes
├── service/             # interfaces + impl/ subdirectory
├── repository/          # Spring Data JPA interfaces
├── domain/
│   ├── entity/          # JPA @Entity classes
│   ├── dto/             # Request/Response DTOs
│   └── mapper/          # MapStruct @Mapper interfaces
├── exception/           # Custom exceptions + GlobalExceptionHandler
├── security/            # SecurityConfig, JWT filter if custom
├── messaging/           # JMS producers + consumers (if applicable)
│   ├── producer/
│   └── consumer/
└── util/                # Utility classes (stateless, final)
```

### Step 3 — Design API Contracts

For **every endpoint** derived from the LRS functional requirements:

**Specify:**
- HTTP method and path (RESTful, plural nouns, kebab-case)
- Auth requirement (OAuth2 scope)
- Request body (all fields with type, validation constraints, example)
- Success response (status code, body, example)
- All error responses (status codes, RFC 7807 ProblemDetail format)
- Idempotency handling (if state-changing)
- Rate limiting considerations

**Use this endpoint block format:**

```
### {METHOD} {path}
**Summary:** {one line}
**Auth:** Bearer token, scope: {scope}
**Idempotency:** {YES - via X-Idempotency-Key header / NO}

Request Body:
  {field}: {type} — {validation} — {description}

Response 201/200:
  {field}: {type} — {description}

Errors:
  400 — {condition}
  401 — Missing or invalid token
  404 — {condition} (GET/PUT/DELETE only)
  409 — {condition} (idempotency / duplicate)
  500 — Unhandled exception
```

### Step 4 — Design Database Schema

For each JPA entity:
- Table name (snake_case, plural)
- All columns with type, constraints, nullability
- Primary key (UUID default, never auto-increment integer)
- Indexes (state the query pattern each index supports)
- Foreign keys and relationships
- Audit columns: `created_at`, `updated_at`, `created_by`, `version` (optimistic lock)
- Soft delete column: `deleted_at` (never physically delete business data)

Output as:
1. Mermaid ER diagram
2. SQL DDL CREATE TABLE statement (PostgreSQL 15 syntax)
3. Flyway migration file name: `V1__{description}.sql`

### Step 5 — Design Class Diagram

Produce a Mermaid class diagram showing:
- All controllers, services (interface + impl), repositories, entities, DTOs, mappers
- Relationships (dependency, implements, extends)
- Key method signatures on each class

```mermaid
classDiagram
    class {Resource}Controller {
        -{Resource}Service service
        +create(RequestDto) ResponseDto
        +getById(UUID) ResponseDto
    }
    ...
```

### Step 6 — Design Sequence Diagrams

Produce a Mermaid sequence diagram for **every acceptance criteria scenario** from the LRS:
- Happy path (full flow from client to DB to MQ to response)
- Validation failure path
- System error path (DB down, MQ unavailable)
- Idempotency path (if applicable)

### Step 7 — Design Error Handling

Define the complete exception hierarchy:
- Base exception class
- Domain exceptions (NotFound, Duplicate, Invalid, etc.)
- Integration exceptions (ExternalService, MQPublish, etc.)
- GlobalExceptionHandler mapping table (exception → HTTP status → log level → alert)

### Step 8 — Design Messaging (if applicable)

For each MQ topic/queue:
- Name (company naming convention: `COMPANY.{SERVICE}.{EVENT}`)
- Direction (inbound / outbound)
- Message schema (JSON with all fields typed)
- Retry strategy (attempts, backoff, DLQ name)
- DLQ handling (alert threshold, reprocessing strategy)

### Step 9 — Define Configuration & Secrets

Table of all configuration properties:
- Property key
- Source (ConfigMap / Vault / AWS Secrets Manager)
- Sensitive (YES / NO)
- Example value (never real values)

### Step 10 — Assemble and Output

Output using structure in `references/LLD-output-format.md`.
Set Status to `DRAFT`.

---

## Quality Checklist

- [ ] Every LRS functional requirement maps to at least one API endpoint or database operation
- [ ] Every endpoint has: auth, request body, success response, all error responses
- [ ] All error responses use RFC 7807 ProblemDetail format
- [ ] Database schema has: UUID PK, optimistic lock version, audit timestamps
- [ ] Flyway migration script named correctly
- [ ] Class diagram covers all layers (controller → service → repository → entity)
- [ ] Sequence diagram covers happy path AND at least one failure path per use case
- [ ] No hardcoded values in configuration table — all secrets point to Vault or AWS SM
- [ ] Logging standard applied: SLF4J, MDC traceId on all methods
- [ ] LRS-ID and HLD-ID referenced in document header

---

## Rules

- **Every state-changing endpoint must have an idempotency strategy**
- **Never use auto-increment integer PKs** — always UUID
- **Never physically delete business data** — use soft delete with `deleted_at`
- **All DTOs must have validation annotations** — never accept raw unvalidated input
- **GlobalExceptionHandler must handle ALL exception types** — no uncaught exceptions reach the client
- **MQ failure must not cause synchronous API failure** where business rules permit async
- **Configuration must separate concerns** — no business logic in application.yml

---

## Reference Files

- `references/LLD-output-format.md` — full LLD structure template
- `references/api-design-standards.md` — REST naming, versioning, error format rules
- `references/db-schema-standards.md` — column naming, type choices, index patterns
