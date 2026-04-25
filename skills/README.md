# AI-SDLC Skills Library

> Reusable AI skill definitions for every phase of the AI-augmented SDLC framework.
> Each skill is a self-contained `SKILL.md` that tells Claude (or any AI) exactly how to perform a specific task — consistently, to company standards, every time.

---

## How to Use a Skill

1. Open your AI tool (Claude, Copilot Chat, IntelliJ AI Assistant)
2. Paste the contents of the relevant `SKILL.md` into the system prompt or as context
3. Provide the required inputs described in the skill
4. Review and approve the output before using it downstream

---

## Skill Index

| # | Skill | Phase | Purpose |
|---|-------|-------|---------|
| 01 | [parse-requirements](./01-parse-requirements/SKILL.md) | 01 | Extract structured LRS from any requirement source |
| 02 | [generate-hld](./02-generate-hld/SKILL.md) | 02 | Generate High-Level Design from a locked LRS |
| 03 | [generate-lld](./03-generate-lld/SKILL.md) | 02 | Generate Low-Level Design from an approved HLD |
| 04 | [generate-openapi](./04-generate-openapi/SKILL.md) | 02 | Generate OpenAPI 3.0 spec from locked LLD — API contract gate before code |
| 05 | [generate-stories](./05-generate-stories/SKILL.md) | 03 | Decompose LLD + OpenAPI spec into Jira-ready user stories |
| 06 | [generate-code-scaffold](./06-generate-code-scaffold/SKILL.md) | 04 | Generate Spring Boot scaffold driven by locked OpenAPI spec + LLD |
| 07 | [generate-unit-tests](./07-generate-unit-tests/SKILL.md) | 05 | Generate comprehensive JUnit 5 unit tests |
| 08 | [review-code-security](./08-review-code-security/SKILL.md) | 04 | OWASP-aligned security review of Java code |
| 09 | [generate-perf-tests](./09-generate-perf-tests/SKILL.md) | 05 | Generate Gatling performance simulations from NFRs |
| 10 | [generate-release-notes](./10-generate-release-notes/SKILL.md) | 07 | Generate structured release notes from Git/Jira |

---

## Skill Chaining (Recommended Flow)

Top-down approach: the OpenAPI specification is the contract gate between design and code.
No code is generated until the OpenAPI spec is locked and approved.

```
Input: Jira Epic / Confluence / PRD
         │
         ▼
    [01] parse-requirements  →  Locked LRS (LRS-ID assigned)
         │
         ▼
    [02] generate-hld        →  HLD document (Architect sign-off)
         │
         ▼
    [03] generate-lld        →  LLD document (Engineer sign-off)
         │
         ▼
    [04] generate-openapi    →  Locked OpenAPI 3.0 spec ◄── API CONTRACT GATE
         │
         ├──► [05] generate-stories      →  Jira backlog (references spec)
         │
         ├──► [06] generate-code-scaffold →  Spring Boot scaffold (driven by spec)
         │         │
         │         ├──► [07] generate-unit-tests
         │         └──► [08] review-code-security
         │
         ├──► [09] generate-perf-tests   →  Gatling simulations
         │
         └──► [10] generate-release-notes → Release documentation
```

---

## Skill Versioning

Each skill tracks its own version in the YAML frontmatter. When a skill is updated:
- Bump the version
- Add a changelog entry
- Tag the Git commit: `skill/parse-requirements@v1.1.0`

---

## Contributing a New Skill

1. Copy the `_template/` folder
2. Fill in the SKILL.md following the schema
3. Add at least 2 worked examples in `examples/`
4. Open a PR — peer review required before merge
5. Update this README index
