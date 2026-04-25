---
name: parse-requirements
version: 1.0.0
phase: "01 — Requirement Intake"
description: >
  Extracts and structures requirements from any source artifact into a
  Locked Requirement Specification (LRS). Use this skill whenever a user
  provides a Jira Epic, Confluence page, PRD, email thread, meeting transcript,
  or any other raw requirement artifact and wants it parsed, structured, or
  converted into a formal LRS. Also trigger when a user says "parse this
  requirement", "turn this into an LRS", "extract requirements from", "analyse
  this epic", or "what are the requirements here". Always use this skill before
  any design or story work begins — never skip requirement structuring.
inputs:
  - name: source_artifact
    description: Raw text from Jira Epic, Confluence, PRD, transcript, or email
    required: true
  - name: source_type
    description: "One of: jira_epic | confluence | prd | email | transcript | other"
    required: false
    default: "other"
  - name: service_name
    description: Name of the service or system being described
    required: false
output: Structured Locked Requirement Specification (LRS) in Markdown
---

# Skill: Parse Requirements → LRS

## Purpose

Transform any raw requirement artifact into a structured, traceable Locked Requirement Specification (LRS) that downstream phases (design, stories, code, tests) can reliably build on.

---

## Step-by-Step Instructions

### Step 1 — Read and Understand the Source

Read the entire source artifact before extracting anything. Identify:
- The **domain** (payments, SSI, notifications, etc.)
- The **primary actor** (who uses this system)
- The **core business goal** (what problem is being solved)
- Any **technical constraints** already mentioned

### Step 2 — Extract Requirements

Go through the artifact systematically and extract:

**Functional Requirements (FR)**
- Assign each a unique ID: `LRS-{YYYYMMDD}-FR-{nn}` (e.g. `LRS-20240115-FR-01`)
- Must be testable — if a requirement cannot be tested, flag it with `[CLARIFICATION NEEDED]`
- Use SHALL for mandatory, SHOULD for recommended, MAY for optional

**Non-Functional Requirements (NFR)**
- Performance: extract any numbers mentioned (response times, TPS, SLAs)
- If no numbers given, apply sensible defaults and flag them: `[ASSUMED: P95 < 500ms — confirm with architect]`
- Categories: Performance, Availability, Scalability, Security, Compliance, DR/RTO/RPO

**Integration Requirements**
- List every upstream and downstream system mentioned
- For each: direction (inbound/outbound), protocol (REST/MQ/DB/SFTP), auth mechanism

**Security Requirements**
- Authentication and authorisation model
- Data classification (PUBLIC / INTERNAL / CONFIDENTIAL / RESTRICTED / PII)
- Compliance requirements (GDPR, PCI-DSS, SOX, FCA)
- Audit logging needs

**Constraints**
- Technology constraints (must use Java, must deploy on OCP, etc.)
- Timeline, budget, or team capacity constraints
- Existing system compatibility requirements

### Step 3 — Identify Ambiguities

After extraction, re-read the source and list every ambiguity, gap, or assumption. For each:
- Assign ID: `Q-{nn}`
- State what is unclear
- Suggest the most likely intent (so the human only needs to confirm, not invent)
- Mark as: `[ASSUMPTION]` if you have inferred a value, or `[CLARIFICATION NEEDED]` if you cannot infer

Surface **at least 5 questions** — if fewer than 5 genuine ambiguities exist, note that the requirement is unusually complete.

### Step 4 — Assign LRS ID

Generate a unique LRS ID in format: `LRS-{YYYYMMDD}-{3-digit-sequence}`
Use today's date. Sequence starts at 001 per day.

### Step 5 — Produce the LRS Document

Output the full LRS using the template in `references/LRS-output-format.md`.

Set Status to `DRAFT` — it becomes `LOCKED` only after human sign-off.

### Step 6 — Produce Traceability Seed

After the LRS, output a traceability matrix seed table:

```markdown
| LRS-ID | Requirement Summary | HLD Ref | LLD Ref | Story ID | Test ID | Status |
|--------|--------------------|---------|---------|-----------|---------|----|
| LRS-YYYYMMDD-FR-01 | {short summary} | — | — | — | — | DRAFT |
```

Leave HLD Ref, LLD Ref, Story ID, Test ID blank — filled in by downstream phases.

---

## Quality Checklist

Before outputting, verify:
- [ ] Every functional requirement has a unique ID
- [ ] Every FR is testable (no vague language like "fast" or "user-friendly" without metrics)
- [ ] All NFRs have measurable thresholds or are flagged as assumed
- [ ] Security section covers: auth, authz, data classification, audit logging
- [ ] All integration points have: system name, direction, protocol, auth
- [ ] Out-of-scope section is present (even if short)
- [ ] At least 5 open questions listed
- [ ] Status is `DRAFT`
- [ ] LRS-ID assigned

---

## Rules

- **Never invent requirements** — only extract what is present or clearly implied
- **Always flag assumptions** — use `[ASSUMED: ...]` inline
- **Never set status to LOCKED** — that requires human sign-off
- **If the source is a transcript or email**, note any conflicting statements between speakers/participants
- **If PII or financial data is involved**, always flag the data classification as CONFIDENTIAL or RESTRICTED minimum

---

## Output Format

Follow the structure in `references/LRS-output-format.md` exactly.

Key sections in order:
1. Document header (LRS-ID, version, status, source link)
2. Business Context
3. Functional Requirements (table with ID, requirement, priority, notes)
4. Non-Functional Requirements
5. Integration Requirements
6. Security Requirements
7. Constraints
8. Out of Scope
9. Assumptions & Dependencies
10. Open Questions (Clarification Log)
11. Approval Sign-Off (blank — for humans to complete)
12. Traceability Matrix Seed

---

## Examples

See `examples/` for worked examples:
- `examples/01-jira-epic-input.md` — raw Jira Epic → LRS
- `examples/02-confluence-input.md` — Confluence page → LRS
- `examples/03-email-thread-input.md` — email chain → LRS

---

## Reference Files

- `references/LRS-output-format.md` — full LRS template with field descriptions
- `references/requirement-quality-rules.md` — rules for testable, well-formed requirements
