# Claude Project Context — AI-SDLC Framework

> Paste this file at the start of every Claude session in IntelliJ to give Claude
> full context of this project. You can do this by opening this file, selecting all,
> and pasting it into the Claude chat panel before asking anything.

---

## What This Project Is

This is the **AI-SDLC Framework** repository for [Company Name].
It defines the end-to-end AI-augmented software delivery process for Java/Spring Boot
microservices deployed on Amazon EKS and OpenShift OCP.

Every phase of the SDLC has a corresponding folder with templates, scripts, and a
**skill** (`SKILL.md`) that tells Claude exactly how to perform that task.

---

## Repository Structure

```
ai-sdlc-framework/
├── .claude/
│   └── project.md          ← YOU ARE HERE — paste this at session start
├── 01-requirement-intake/   Phase 01: LRS templates and AI prompt templates
├── 02-design-documents/     Phase 02: HLD and LLD templates
├── 03-story-creation/       Phase 03: Story templates, Jira API script
├── 04-code-development/     Phase 04: Coding standards, Checkstyle, code prompts
├── 05-automated-testing/    Phase 05: Test strategy
├── 06-cicd-pipeline/        Phase 06: Jenkinsfile, Harness pipeline YAML
├── 07-deployment/           Phase 07: Helm charts (EKS + OCP), Argo Rollouts
├── 08-governance/           Phase 08: Approval gates, audit trail
├── docs/                    Interactive HTML framework document
└── skills/                  ← AI SKILLS — one per major task
    ├── README.md
    ├── 01-parse-requirements/SKILL.md
    ├── 02-generate-hld/SKILL.md
    ├── 03-generate-lld/SKILL.md
    ├── 04-generate-stories/SKILL.md
    ├── 05-generate-code-scaffold/SKILL.md
    ├── 06-generate-unit-tests/SKILL.md
    ├── 07-generate-openapi/SKILL.md
    ├── 08-review-code-security/SKILL.md
    ├── 09-generate-perf-tests/SKILL.md
    └── 10-generate-release-notes/SKILL.md
```

---

## Technology Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.x (Jakarta namespace — always `jakarta.*` not `javax.*`)
- **Build:** Maven 3.9+
- **Database:** PostgreSQL 15 via Spring Data JPA + Flyway
- **Messaging:** IBM MQ 9.4 via Spring JMS
- **Security:** Spring Security OAuth2 Resource Server (JWT)
- **Container:** Docker → Amazon EKS / OpenShift OCP
- **CI/CD:** Jenkins (Declarative Pipeline) or Harness
- **Observability:** Prometheus + Grafana + Jaeger + Loki

---

## Coding Standards (Always Apply These)

1. **Lombok always** — `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`. No manual getters/setters.
2. **Constructor injection always** — never `@Autowired` field injection
3. **Jakarta namespace** — `jakarta.persistence.*`, `jakarta.validation.*` (NOT javax.*)
4. **DTOs only from controllers** — never return JPA entities from APIs
5. **RFC 7807 ProblemDetail** — all error responses use Spring 6 ProblemDetail
6. **MDC traceId** — every log statement must include correlation ID
7. **UUID primary keys** — `gen_random_uuid()`, never auto-increment integers
8. **Soft delete** — `deleted_at` column, never physical delete of business data
9. **@Version** on all JPA entities for optimistic locking
10. **No hardcoded secrets** — always Vault / AWS Secrets Manager via environment variables

---

## Package Structure Convention

```
com.{company}.{domain}.{service}/
├── config/          @Configuration classes
├── controller/      @RestController (thin layer, no business logic)
├── service/         interfaces + impl/ subdirectory
├── repository/      Spring Data JPA interfaces
├── domain/
│   ├── entity/      @Entity classes
│   ├── dto/         Request/Response DTOs with @Valid
│   └── mapper/      MapStruct @Mapper interfaces
├── exception/       Custom exceptions + GlobalExceptionHandler
├── security/        SecurityConfig
├── messaging/       producer/ + consumer/ subdirectories
└── util/            Stateless utility classes
```

---

## How to Use Skills in IntelliJ

Each skill is a SKILL.md file in `skills/`. To use one:

1. Open the relevant `SKILL.md` in IntelliJ
2. Select all content (Ctrl+A)
3. Paste into the Claude chat panel
4. Provide the required inputs listed at the top of the skill
5. Review and validate Claude's output before using it

### Quick Skill Reference

| Task | Skill to Use | Required Input |
|------|-------------|----------------|
| Parse a Jira Epic into an LRS | `skills/01-parse-requirements/SKILL.md` | Raw epic text |
| Create HLD from LRS | `skills/02-generate-hld/SKILL.md` | Locked LRS |
| Create LLD from HLD | `skills/03-generate-lld/SKILL.md` | Approved HLD + LRS |
| Generate OpenAPI spec | `skills/04-generate-openapi/SKILL.md` | Locked LLD + service name |
| Generate Jira stories | `skills/05-generate-stories/SKILL.md` | LRS + LLD + OpenAPI spec |
| Generate Spring Boot scaffold | `skills/06-generate-code-scaffold/SKILL.md` | Locked OpenAPI spec + LLD |
| Generate unit tests | `skills/07-generate-unit-tests/SKILL.md` | Service class + ACs |
| Security review code | `skills/08-review-code-security/SKILL.md` | Java source code |
| Generate Gatling perf tests | `skills/09-generate-perf-tests/SKILL.md` | LRS NFRs + LLD APIs |
| Generate release notes | `skills/10-generate-release-notes/SKILL.md` | Git log + Jira stories |

---

## Traceability Rule (Always Enforce)

Every artifact must reference its upstream LRS requirement ID:
```
LRS-{YYYYMMDD}-{seq}   →   HLD section   →   LLD section
                        →   Story (Jira)  →   Code (PR)
                        →   Test case     →   Deployment
```

If Claude generates anything without an LRS reference, ask it to add one.

---

## Quality Gates (Never Bypass)

| Gate | Threshold |
|------|-----------|
| Unit test coverage | ≥ 85% |
| SonarQube Quality Gate | Passed (0 blockers, 0 criticals) |
| CVE scan (OWASP + Trivy) | 0 HIGH/CRITICAL |
| Secret scan (Gitleaks) | 0 findings |
| Integration tests | 100% pass |
| Canary error rate | < 1% |

---

## Current Active Work (Update This Section Per Project)

```
Service Name:     {e.g. payment-service}
Package Base:     {e.g. com.company.payments}
LRS Reference:    {e.g. LRS-20240115-001}
Sprint:           {e.g. Sprint 12}
Jira Epic:        {e.g. PAY-100}
Active Phase:     {e.g. Phase 04 — Code Development}
```

---

## Session Workflow

### Starting a new session — paste this sequence:
1. This file (`project.md`) — gives Claude full project context
2. The relevant `SKILL.md` — tells Claude how to do the specific task
3. Your input (LRS / LLD / source code / etc.)

### Example session start:
```
[Paste project.md]
[Paste skills/05-generate-code-scaffold/SKILL.md]

I need you to generate a Spring Boot scaffold for the payment service.
Service name: payment-service
Package: com.company.payments
Entity: Payment

Here is the LLD:
[Paste LLD content]
```

---

*Version: 1.0.0 | Keep this file updated as the project evolves.*
