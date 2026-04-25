# Locked Requirement Specification (LRS)

> **Status:** `DRAFT` | `UNDER_REVIEW` | `LOCKED`
> **Version:** 1.0.0
> **LRS-ID:** LRS-{YYYYMMDD}-{sequence}
> **Locked By:** {Name} — {Date}
> **Source Artifact:** {Jira Epic / Confluence URL / PRD link}

---

## 1. Business Context

| Field | Value |
|-------|-------|
| Business Objective | |
| Target User / Persona | |
| Business Value | |
| Success Metric | |
| Deadline / Target Sprint | |
| Priority | HIGH / MEDIUM / LOW |

---

## 2. Functional Requirements

> Each requirement must have a unique ID in format `LRS-{seq}-FR-{n}`

| ID | Requirement | Priority | Notes |
|----|------------|----------|-------|
| LRS-001-FR-01 | | MUST | |
| LRS-001-FR-02 | | SHOULD | |
| LRS-001-FR-03 | | COULD | |

---

## 3. Non-Functional Requirements (NFRs)

| Category | Requirement | Metric / Threshold |
|----------|------------|-------------------|
| Performance | Response time | P95 < 500ms under 100 TPS |
| Availability | Uptime | 99.9% SLA |
| Scalability | Horizontal scaling | Support 10x current load |
| Security | Authentication | OAuth2 / OIDC enforced |
| Data Retention | Audit logs | Minimum 7 years |
| Recovery | RTO / RPO | RTO < 1hr, RPO < 15min |

---

## 4. Integration Requirements

| System | Direction | Protocol | Auth | Notes |
|--------|-----------|----------|------|-------|
| | Inbound / Outbound | REST / MQ / DB | OAuth2 / API Key | |

---

## 5. Security Requirements

- [ ] Authentication mechanism defined (OAuth2 / OIDC / API Key)
- [ ] Authorization model defined (RBAC / ABAC)
- [ ] Data classification level: `PUBLIC` / `INTERNAL` / `CONFIDENTIAL` / `RESTRICTED`
- [ ] PII data involved: YES / NO — if YES, masking/encryption approach defined
- [ ] Audit logging required: YES / NO
- [ ] Specific compliance requirements (PCI-DSS, GDPR, SOX): {list}

---

## 6. Constraints

| Constraint | Description |
|-----------|-------------|
| Technology | Must use Java 17 + Spring Boot 3.x |
| Platform | Must deploy on EKS or OCP |
| Existing Systems | Must not break {system} API contract |
| Budget / Capacity | |
| Regulatory | |

---

## 7. Out of Scope

Explicitly list what is NOT included in this requirement to prevent scope creep:

- {Item 1}
- {Item 2}

---

## 8. Assumptions & Dependencies

| Type | Description | Owner | Resolution Date |
|------|------------|-------|----------------|
| Assumption | | | |
| Dependency | | | |
| Risk | | | |

---

## 9. Open Questions (Clarification Log)

> AI-surfaced ambiguities requiring human resolution before this LRS can be locked.

| ID | Question | Raised By | Answer | Resolved By | Date |
|----|---------|-----------|--------|-------------|------|
| Q-01 | | AI | | | |
| Q-02 | | AI | | | |

---

## 10. Approval Sign-Off

| Role | Name | Signature / Approval | Date |
|------|------|----------------------|------|
| Product Owner | | ☐ Approved | |
| Solution Architect | | ☐ Approved | |
| Security Reviewer | | ☐ Approved | |
| Engineering Lead | | ☐ Approved | |

---

> ⚠️ **Once status is set to LOCKED, no changes are permitted without raising a Change Request (CR) and incrementing the version number. All downstream artifacts (HLD, LLD, Stories, Tests) must reference this LRS-ID.**
