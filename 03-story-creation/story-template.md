# User Story Template

> Copy this template for each story. Stories are auto-created in Jira via `jira-api-script.py`.

---

## Story Header

| Field | Value |
|-------|-------|
| **Story ID** | {Jira Story ID — auto-assigned} |
| **Epic Link** | {Jira Epic ID} |
| **LRS Reference** | LRS-{ID}-FR-{n} |
| **Component** | {service-name} |
| **Sprint Target** | Sprint {n} |
| **Story Points** | {1 / 2 / 3 / 5 / 8} |
| **Priority** | Highest / High / Medium / Low |
| **Labels** | `LRS-{id}`, `{component}`, `backend` |

---

## Story Title

> As a **{persona}**, I want to **{goal}** so that **{benefit}**.

---

## Background / Context

> Optional. 2-3 sentences providing context for the developer. Link to LLD section.

---

## Acceptance Criteria

> Written in Given/When/Then (Gherkin) format. Minimum 2 scenarios.

### Scenario 1: Happy Path

```gherkin
Given {precondition}
When  {action is performed}
Then  {expected outcome}
And   {additional assertions}
```

### Scenario 2: Validation / Error Path

```gherkin
Given {invalid input or error condition}
When  {action is performed}
Then  {error response is returned}
And   {system state is unchanged}
```

### Scenario 3: Edge Case (if applicable)

```gherkin
Given {edge case condition}
When  {action is performed}
Then  {expected behaviour}
```

---

## Definition of Done

- [ ] Code written and reviewed by ≥1 senior engineer
- [ ] Unit tests written with ≥85% coverage for new code
- [ ] Integration tests written and passing
- [ ] SonarQube Quality Gate: Passed (0 blockers, 0 criticals)
- [ ] OWASP dependency check: 0 HIGH/CRITICAL CVEs
- [ ] No secrets hardcoded (Gitleaks clean)
- [ ] API contract matches approved OpenAPI spec
- [ ] Logging includes correlation ID (traceId)
- [ ] Swagger / OpenAPI doc updated
- [ ] PR reviewed and merged to main
- [ ] Deployed and verified in DEV environment
- [ ] Acceptance criteria demonstrated to PO

---

## Non-Functional Notes

> Note any NFR implications for this story.

- **Performance:** Expected P95 response time for this endpoint: < {X}ms
- **Security:** {Any OWASP Top 10 considerations for this story}
- **Scalability:** {Any state or concurrency considerations}

---

## Technical Tasks

> Sub-tasks to be created in Jira under this story.

| Task | Assignee | Estimate |
|------|---------|---------|
| Implement {Controller / Service / Repository} | | {hours} |
| Write unit tests | | {hours} |
| Write integration tests | | {hours} |
| Update OpenAPI spec | | {hours} |
| Update Confluence docs | | {hours} |

---

## Dependencies / Blockers

| Dependency | Type | Story / Team | Status |
|-----------|------|-------------|--------|
| | Blocked by / Depends on | | Open / Resolved |

---

## Notes / Clarifications

> Any additional notes for the developer or QA engineer.
