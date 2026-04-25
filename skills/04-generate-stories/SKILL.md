---
name: generate-stories
version: 1.0.0
phase: "03 — Story Creation"
description: >
  Decomposes a locked LRS and approved LLD into a complete, sprint-ready Jira
  backlog of Epics, Stories, and Tasks with acceptance criteria, story points,
  and full LRS traceability. Use this skill whenever a user wants to create
  user stories, break down requirements into tickets, generate a Jira backlog,
  write acceptance criteria, or decompose an epic into stories. Trigger when
  the user says "create stories", "generate backlog", "write user stories",
  "break this into tickets", "create Jira stories", "write ACs", or "decompose
  this epic". Always generate stories from the LLD — never from raw requirements
  directly. Stories without LRS traceability IDs are not acceptable.
inputs:
  - name: lrs
    description: Locked Requirement Specification (must be LOCKED status)
    required: true
  - name: lld
    description: Approved Low-Level Design document
    required: true
  - name: sprint_capacity
    description: "Estimated team capacity in story points per sprint (default: 40)"
    required: false
    default: "40"
  - name: jira_project_key
    description: "Jira project key for ID generation, e.g. PAY"
    required: false
output: Complete backlog in Markdown + optional YAML for jira-api-script.py
---

# Skill: Generate Stories from LLD

## Purpose

Produce a complete, PO-reviewable sprint backlog from the LLD and LRS. Every story must be INVEST-compliant, traceable, and implementable by a senior engineer without further clarification.

---

## Step-by-Step Instructions

### Step 1 — Identify Epics

Map LRS sections to Epics. Each Epic covers a distinct business capability:
- One Epic per major LRS functional area
- Epic title: business-language (not technical)
- Epic contains: business goal, target persona, acceptance criteria at epic level, LRS reference

**Epic format:**
```
EPIC: {Business-Language Title}
LRS Ref: LRS-{date}-FR-{nn} to FR-{nn}
Goal: As a {persona}, I want {capability} so that {business value}
AC: {high-level acceptance criteria}
Components: {service-name}
Labels: LRS-{id}, {component}, {domain}
```

### Step 2 — Decompose Each Epic into Stories

For each LRS functional requirement and each LLD API endpoint/flow, generate a Story.

**Rules:**
- One Story = one deliverable, testable increment
- If a Story would exceed 8 story points, split it
- Stories must be **Independent** — avoid dependencies where possible; flag unavoidable ones
- Each Story must be completable in one sprint

**Size guide:**
| Size | Points | Scope |
|------|--------|-------|
| XS | 1 | Trivial change, minimal logic |
| S | 2 | Simple CRUD endpoint, no complex logic |
| M | 3 | Endpoint with business rules and unit tests |
| L | 5 | Complex endpoint with integration, events |
| XL | 8 | Large feature — consider splitting |

### Step 3 — Write Each Story

For every Story, produce:

**Header block:**
```
STORY: {Jira story title — start with action verb}
LRS Ref: LRS-{date}-FR-{nn}
LLD Ref: Section {n} — {title}
Epic: {Epic title}
Points: {1/2/3/5/8}
Priority: {Highest/High/Medium/Low}
Component: {service-name}
Labels: LRS-{date}-FR-{nn}, {component}, backend
Sprint Target: Sprint {n}
```

**Story statement:**
```
As a {persona},
I want to {goal — specific, actionable},
So that {benefit — business value, not technical detail}.
```

**Background (optional, 2-3 sentences max):**
Point to the relevant LLD section. Include the API endpoint or DB operation. Note any important constraints.

**Acceptance Criteria — Given/When/Then (minimum 2 scenarios):**

Scenario 1: Happy Path
```gherkin
Given {precondition — system state, auth token scope}
When  {specific action performed, with inputs}
Then  {verifiable outcome — response code, body, DB state, MQ event}
And   {additional assertions}
```

Scenario 2: Failure / Validation
```gherkin
Given {invalid input or error condition}
When  {action performed}
Then  {error response with status code and RFC 7807 body}
And   {no side effects — no DB write, no MQ publish}
```

Add more scenarios for: idempotency, auth failure (401), auth (403), not found (404) where relevant.

**Definition of Done:**
Use this standard DoD for every story — do not abbreviate or shorten it:
```
- [ ] Code written and peer-reviewed by ≥1 senior engineer
- [ ] Unit tests written — coverage ≥85% for new code
- [ ] Integration test covering all AC scenarios
- [ ] SonarQube Quality Gate: Passed
- [ ] 0 HIGH/CRITICAL CVEs in new dependencies (OWASP scan)
- [ ] No hardcoded secrets (Gitleaks clean)
- [ ] API response matches approved OpenAPI spec
- [ ] Logging includes MDC traceId on all log statements
- [ ] OpenAPI / Swagger doc updated
- [ ] PR merged to main after peer review
- [ ] Deployed and verified in DEV environment
- [ ] All AC scenarios demonstrated to PO
```

**NFR Note:**
State the relevant NFR for this story:
```
NFR: Response time for this endpoint: P95 < {X}ms
Security: {relevant OWASP consideration, e.g. "Input validated — A03 Injection"}
```

**Technical Sub-tasks:**
Break into tasks an engineer can pick up independently:
```
TASK: Implement {Controller/Service/Repository layer}
TASK: Write unit tests for {class}
TASK: Write integration test — {scenario}
TASK: Create Flyway migration V{n}__{description}.sql (if DB change)
TASK: Update OpenAPI spec
```

### Step 4 — Identify and Flag Dependencies

After generating all stories, produce a dependency matrix:

| Story | Depends On | Type | Impact |
|-------|-----------|------|--------|
| {Story title} | {Other story} | Blocked by | Cannot start until {other} is merged |

Flag any external dependencies (other teams, services, environments).

### Step 5 — Sprint Allocation

Given the `sprint_capacity`, suggest a sprint allocation:

| Sprint | Stories | Total Points | Notes |
|--------|---------|-------------|-------|
| Sprint 1 | {list story titles} | {total} | Foundation stories first |
| Sprint 2 | {list} | {total} | |

Put foundation stories (DB migrations, base service setup) in Sprint 1 always.

### Step 6 — Output Formats

Produce two outputs:

**A) Markdown (human-readable for PO review)**
Full story details as specified above.

**B) YAML (for jira-api-script.py)**
Follow the schema in `references/stories-yaml-schema.md` exactly.

```yaml
epics:
  - title: "{Epic title}"
    description: "{Epic description}"
    priority: "High"
    labels: ["LRS-{date}", "{component}"]
    stories:
      - title: "As a {persona}, I want to {goal}"
        lrs_ref: "LRS-{date}-FR-{nn}"
        priority: "High"
        story_points: {n}
        labels: ["backend", "{component}", "LRS-{date}-FR-{nn}"]
        background: "{background text}"
        nfr_notes: "{NFR text}"
        acceptance_criteria:
          - title: "{Scenario title}"
            gherkin: |
              Given ...
              When ...
              Then ...
        tasks:
          - title: "{Task title}"
            description: "{Task description}"
```

---

## Quality Checklist

- [ ] Every LRS functional requirement maps to at least one Story
- [ ] Every Story has a unique LRS traceability ID
- [ ] Every Story has minimum 2 AC scenarios (happy path + failure)
- [ ] All AC scenarios use Given/When/Then format
- [ ] AC scenarios reference specific HTTP status codes and response formats
- [ ] Every Story has the standard DoD (not shortened)
- [ ] Every Story has an NFR note
- [ ] No Story exceeds 8 story points (split if necessary)
- [ ] YAML output is valid (no syntax errors)
- [ ] Sprint allocation respects stated team capacity

---

## Rules

- **Never write ACs in vague language** — "system works correctly" is not acceptable
- **Never omit the failure scenario** — every happy path needs at least one failure AC
- **Never use technical jargon in the Story title or statement** — business language only
- **Never create Stories without LRS traceability** — if you cannot trace to LRS, flag it
- **Foundation always goes first** — DB setup, base service, security config before feature stories

---

## Reference Files

- `references/stories-yaml-schema.md` — YAML schema for jira-api-script.py
- `references/invest-checklist.md` — INVEST principle definitions and checks
- `references/ac-writing-guide.md` — How to write unambiguous Given/When/Then ACs
