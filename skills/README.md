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
| 04 | [generate-stories](./04-generate-stories/SKILL.md) | 03 | Decompose LLD into Jira-ready user stories |
| 05 | [generate-code-scaffold](./05-generate-code-scaffold/SKILL.md) | 04 | Generate Spring Boot service scaffold from LLD |
| 06 | [generate-unit-tests](./06-generate-unit-tests/SKILL.md) | 05 | Generate comprehensive JUnit 5 unit tests |
| 07 | [generate-openapi](./07-generate-openapi/SKILL.md) | 04 | Generate OpenAPI 3.0 spec from LLD API contracts |
| 08 | [review-code-security](./08-review-code-security/SKILL.md) | 04 | OWASP-aligned security review of Java code |
| 09 | [generate-perf-tests](./09-generate-perf-tests/SKILL.md) | 05 | Generate Gatling performance simulations from NFRs |
| 10 | [generate-release-notes](./10-generate-release-notes/SKILL.md) | 07 | Generate structured release notes from Git/Jira |

---

## Skill Chaining (Recommended Flow)

```
Input: Jira Epic / Confluence / PRD
         │
         ▼
    [01] parse-requirements  →  Locked LRS (LRS-ID assigned)
         │
         ▼
    [02] generate-hld        →  HLD document (Architect reviews)
         │
         ▼
    [03] generate-lld        →  LLD document + OpenAPI via [07]
         │
         ├──► [04] generate-stories      →  Jira backlog
         │
         ├──► [05] generate-code-scaffold →  Spring Boot scaffold
         │         │
         │         ├──► [06] generate-unit-tests
         │         ├──► [07] generate-openapi
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
