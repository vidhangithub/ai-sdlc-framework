# Claude + IntelliJ — Quick Reference Card

## Every Session Starts With
1. Paste `.claude/project.md` into Claude chat → gives framework context
2. Paste the relevant `SKILL.md` → tells Claude how to do the task
3. Provide your specific input

---

## Skill Cheat Sheet

```
TASK                          SKILL FILE
─────────────────────────────────────────────────────────────
Parse Epic/PRD → LRS          skills/01-parse-requirements/SKILL.md
LRS → HLD                     skills/02-generate-hld/SKILL.md
HLD → LLD                     skills/03-generate-lld/SKILL.md
LLD → Jira Stories            skills/04-generate-stories/SKILL.md
LLD → Java Code Scaffold      skills/05-generate-code-scaffold/SKILL.md
Service Class → Unit Tests    skills/06-generate-unit-tests/SKILL.md
LLD API → OpenAPI YAML        skills/07-generate-openapi/SKILL.md
Java Code → Security Review   skills/08-review-code-security/SKILL.md
LRS NFRs → Gatling Tests      skills/09-generate-perf-tests/SKILL.md
Git Log → Release Notes       skills/10-generate-release-notes/SKILL.md
```

---

## Session Template (Copy-Paste This)

```
=== PROJECT CONTEXT ===
[Paste contents of .claude/project.md]

=== SKILL ===
[Paste contents of relevant SKILL.md]

=== MY REQUEST ===
[Describe what you need]

=== INPUT ===
[Paste your LRS / LLD / code / etc.]
```

---

## Coding Standards Claude Will Always Apply

| Rule | Value |
|------|-------|
| Java | 17 |
| Namespace | `jakarta.*` (never `javax.*`) |
| Injection | Constructor only (never `@Autowired`) |
| DTOs | Always (never entities from APIs) |
| Error format | RFC 7807 `ProblemDetail` |
| Logging | SLF4J + MDC `traceId` |
| Primary Key | UUID (`gen_random_uuid()`) |
| Soft delete | `deleted_at` column |
| Locking | `@Version` on all entities |
| Secrets | Vault / AWS SM (never hardcoded) |

---

## Quality Gates Claude Enforces

- Unit coverage ≥ 85%
- 0 SonarQube blockers/criticals  
- 0 HIGH/CRITICAL CVEs
- 0 hardcoded secrets
- RFC 7807 on all errors
- LRS traceability ID on all artifacts

---

## Branch Naming

```
feature/LRS-{date}-{nn}-{short-desc}
bugfix/LRS-{date}-{nn}-{short-desc}
hotfix/{short-desc}
```

## Commit Format

```
[LRS-{date}-FR-{nn}] feat: add payment creation endpoint
[LRS-{date}-FR-{nn}] fix: handle null amount in validation
[LRS-{date}-FR-{nn}] test: add idempotency test cases
```
