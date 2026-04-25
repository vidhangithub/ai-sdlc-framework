# AI Prompt Templates — Phase 01: Requirement Intake

> Copy these prompts into Claude / ChatGPT / Copilot Chat to process raw requirements into structured LRS format.

---

## Prompt 1: Parse Requirements from Jira Epic

```
You are a Business Analyst assistant. I will provide you with a Jira Epic description.
Your task is to extract and structure the requirements into the following sections:

1. Business Objective (1-2 sentences)
2. Functional Requirements (bullet list, each with a unique ID: FR-01, FR-02...)
3. Non-Functional Requirements (performance, security, availability, scalability)
4. Integration Points (systems to integrate with, direction, protocol)
5. Constraints (technology, regulatory, timeline)
6. Out of Scope (explicitly what is NOT included)
7. Open Questions (ambiguities that need human resolution — list at least 5)

Format the output as structured Markdown.
Flag any assumption you make with [ASSUMPTION].
Flag any missing information with [CLARIFICATION NEEDED].

Here is the Jira Epic:
---
{PASTE EPIC DESCRIPTION HERE}
---
```

---

## Prompt 2: Parse Requirements from Confluence Page

```
You are a Solution Architect assistant. Analyse the following Confluence page content
and produce a structured Locked Requirement Specification (LRS) covering:

- Business context and goals
- Functional requirements with unique IDs (LRS-FR-01...)
- Non-functional requirements with measurable thresholds
- Security requirements (auth, authz, data classification, compliance)
- Integration requirements (upstream/downstream systems)
- Constraints and assumptions
- A list of clarification questions (minimum 5) for the product owner

Use the LRS-template.md format. Mark every assumption explicitly.

Confluence page content:
---
{PASTE CONFLUENCE CONTENT HERE}
---
```

---

## Prompt 3: Parse from Meeting Transcript / Email Thread

```
You are a Requirements Engineer. I will provide you with a meeting transcript or email thread.
Extract all requirement signals from the conversation and produce:

1. Agreed requirements (clearly stated and accepted by participants)
2. Proposed requirements (suggested but not confirmed)
3. Rejected requirements (explicitly ruled out)
4. Conflicting statements (where participants disagreed — flag these)
5. Open action items (things someone agreed to follow up on)
6. Questions for the product owner to resolve

Format as Markdown. Do not invent requirements — only extract what is present.

Transcript / Email:
---
{PASTE HERE}
---
```

---

## Prompt 4: Requirement Completeness Check

```
Review the following draft LRS for completeness and quality. Check:

1. Are all functional requirements testable? (flag any that are vague)
2. Do all NFRs have measurable thresholds? (flag any without numbers)
3. Is the security section complete? (auth, authz, data classification, audit logging)
4. Are all integration points fully described? (system, direction, protocol, auth)
5. Is there a clear out-of-scope section?
6. Are there any contradictions between requirements?
7. What critical information is still missing?

Produce a checklist with PASS / FAIL / NEEDS_CLARIFICATION for each item.

Draft LRS:
---
{PASTE LRS HERE}
---
```

---

## Prompt 5: Generate Traceability Matrix Seed

```
Given the following Locked Requirement Specification, generate a traceability matrix seed
in Markdown table format with columns:

| LRS-ID | Requirement Summary | Design Section | Story ID | Test ID | Status |

Leave Design Section, Story ID, Test ID, and Status blank — these will be filled in
during subsequent phases.

LRS:
---
{PASTE LRS HERE}
---
```
