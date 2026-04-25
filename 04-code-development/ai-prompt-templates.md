# AI Prompt Templates — Phase 04: Code Development

> Use these prompts in GitHub Copilot Chat, VS Code + Claude extension, or IntelliJ AI Assistant.
> Always reference the relevant LLD section when generating code.

---

## Prompt 1: Generate Spring Boot Service Scaffold

```
You are a Java 17 / Spring Boot 3.x expert following enterprise coding standards.

Generate a complete Spring Boot service scaffold for the following:

SERVICE NAME: {service-name}
PACKAGE BASE: com.{company}.{domain}
LRS REFERENCE: LRS-{id}
LLD SECTION: Section 3 (API Contracts) + Section 5 (Class Diagram)

REQUIREMENTS:
- POST /api/v1/{resource} — create a {resource}
- GET  /api/v1/{resource}/{id} — retrieve by UUID
- Auth: Spring Security OAuth2 Resource Server (JWT)
- DB: PostgreSQL via Spring Data JPA + Flyway migration
- Messaging: IBM MQ via Spring JMS (publish event on create)
- Error handling: GlobalExceptionHandler with RFC 7807 ProblemDetail

GENERATE:
1. {Resource}Controller.java — @RestController, @Validated, @Slf4j
2. {Resource}Service.java — interface
3. {Resource}ServiceImpl.java — @Service, @Transactional, @Slf4j
4. {Resource}Repository.java — JpaRepository interface
5. {Resource}.java — @Entity with UUID PK, @Version, audit fields
6. {Resource}RequestDto.java — @Valid annotations, Lombok @Data @Builder
7. {Resource}ResponseDto.java — Lombok @Data @Builder
8. {Resource}Mapper.java — MapStruct @Mapper(componentModel = "spring")
9. GlobalExceptionHandler.java — handle validation, not-found, generic
10. {Resource}NotFoundException.java — extends RuntimeException
11. V1__create_{resource}_table.sql — Flyway migration
12. {Resource}ServiceTest.java — JUnit 5 + Mockito, test all methods

STANDARDS:
- Use Lombok: @Data, @Builder, @RequiredArgsConstructor, @Slf4j
- Use MapStruct for DTO-entity mapping
- MDC correlation ID: traceId on every log line
- No hardcoded values — use @Value or application.yml
- Jakarta namespace (not javax)
- Spring Boot 3.x Actuator health endpoints configured
```

---

## Prompt 2: Generate Unit Tests for Existing Service

```
Generate comprehensive JUnit 5 unit tests for the following Spring Boot service class.

Follow the AAA (Arrange/Act/Assert) pattern.
Use Mockito for all dependencies.
Use AssertJ for assertions.
Cover ALL public methods.
Include these scenarios for each method:
  - Happy path (valid input, success)
  - Validation / null input
  - Repository throws exception
  - Boundary conditions

Test class naming: {ClassName}Test
Display names: @DisplayName with clear business language

Existing service class:
---
{PASTE SERVICE CLASS HERE}
---

Acceptance Criteria to cover:
---
{PASTE AC FROM JIRA STORY}
---
```

---

## Prompt 3: Generate Integration Tests with Testcontainers

```
Generate a Spring Boot integration test class for {resource} creation flow.

Use:
- @SpringBootTest(webEnvironment = RANDOM_PORT)
- @Testcontainers with PostgreSQL 15 container
- @Testcontainers with IBM MQ 9.4 container (image: icr.io/ibm-messaging/mq:9.4.0.0)
- TestRestTemplate or MockMvc for HTTP calls
- Flyway for DB schema setup
- WireMock for external REST service stubs

Cover these scenarios from the acceptance criteria:
1. {AC Scenario 1 — from Jira story}
2. {AC Scenario 2}
3. {AC Scenario 3 — error case}

After each test, verify:
- Database state (direct JDBC check)
- MQ message published (consume from test queue)
- Response body and status code

Service: {service-name}
Package: com.{company}.{domain}.integration
```

---

## Prompt 4: Generate Gatling Performance Test

```
Generate a Gatling 3.x Scala simulation for load testing the {service-name} API.

Endpoint: POST /api/v1/{resource}
Auth: OAuth2 — fetch token from {token-endpoint} before test

Scenario 1 — Baseline Load:
  - 10 users ramp up over 30 seconds
  - Sustain for 2 minutes
  - SLA: P95 < 300ms, error rate < 1%

Scenario 2 — Peak Load:
  - Ramp to 100 users over 60 seconds
  - Sustain for 5 minutes
  - SLA: P95 < 500ms, error rate < 2%

Scenario 3 — Spike Test:
  - 5 users → 200 users in 10 seconds → back to 5 in 10 seconds
  - SLA: no errors, system recovers within 30 seconds

Include:
- Token caching (fetch once, reuse for all users)
- Parameterized request body from feeder CSV
- HTML report generation
- Assertions that fail the simulation if SLAs are breached
```

---

## Prompt 5: Code Security Review

```
Review the following Java Spring Boot code for security vulnerabilities.

Check specifically for:
1. OWASP Top 10 A01 — Broken Access Control (missing auth checks)
2. OWASP A02 — Cryptographic failures (weak algorithms, key handling)
3. OWASP A03 — Injection (SQL, LDAP, XML injection risks)
4. OWASP A05 — Security Misconfiguration (CORS, headers, actuator exposure)
5. OWASP A07 — Identification and Authentication failures
6. OWASP A09 — Security Logging failures (missing audit logs, PII in logs)
7. Hardcoded secrets or credentials
8. Insecure deserialization
9. Missing input validation
10. Race conditions / TOCTOU issues

For each finding, provide:
- Severity: CRITICAL / HIGH / MEDIUM / LOW
- Location: class name + line number
- Description of the vulnerability
- Recommended fix with code example

Code to review:
---
{PASTE CODE HERE}
---
```

---

## Prompt 6: Generate OpenAPI Spec from LLD

```
Generate a complete OpenAPI 3.0.3 YAML specification for the following service.

SERVICE: {service-name}
BASE PATH: /api/v1/{resource}
AUTH: OAuth2 with scopes: read, write, admin

ENDPOINTS (from LLD):
1. POST /api/v1/{resource}
   - Request: {describe fields}
   - Response 201: {describe fields}
   - Response 400: RFC 7807 ProblemDetail
   - Response 401: Unauthorized

2. GET /api/v1/{resource}/{id}
   - Response 200: {describe fields}
   - Response 404: RFC 7807 ProblemDetail

STANDARDS:
- Use $ref for reusable schemas
- Include ProblemDetail schema (RFC 7807)
- Include example values in all schemas
- Include security scheme definition
- Tag all operations with service name
- Include x-correlationId header in all responses
```
