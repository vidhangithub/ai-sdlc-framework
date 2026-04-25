# Governance & Guardrails — AI-SDLC Framework

> **Version:** 1.0.0
> **Owner:** Platform Engineering / Architecture
> **Review Cycle:** Quarterly

---

## 1. Core Governance Principles

1. **AI assists, humans decide** — AI generates drafts and automates repetitive tasks. All critical decisions require human approval.
2. **Traceability is mandatory** — Every artifact must reference its upstream LRS requirement ID.
3. **Security is non-negotiable** — Zero HIGH/CRITICAL findings policy. Pipeline blocks on violations.
4. **Audit trail is immutable** — All approvals, changes, and deployments are logged and version-controlled.
5. **Least privilege** — AI tooling has read/generate access only. It cannot merge, deploy, or approve without human action.

---

## 2. Human Approval Gates

| Gate ID | Gate Name | Phase | Approver Role | What Is Reviewed | SLA |
|---------|-----------|-------|---------------|-----------------|-----|
| GATE-01 | LRS Sign-off | 01 | Architect + Product Owner | Requirement completeness, accuracy, security classification | 2 business days |
| GATE-02 | HLD Approval | 02 | Principal Architect | Architecture decisions, technology choices, security zones | 3 business days |
| GATE-03 | LLD Approval | 02 | Senior Engineer + Architect | API contracts, DB schema, security implementation design | 2 business days |
| GATE-04 | Backlog Approval | 03 | Product Owner | Story quality, acceptance criteria, sprint readiness | 1 business day |
| GATE-05 | PR Review | 04 | Senior Engineer (≥1) | Code quality, security, test coverage, standards compliance | 1 business day |
| GATE-06 | UAT Sign-off | 07 | Business Stakeholder | Functional acceptance in UAT environment | 5 business days |
| GATE-07 | CAB Approval | 07 | Change Advisory Board | Production change risk assessment, rollback plan | Per CAB schedule |

---

## 3. AI Autonomy Boundary

### ✅ What AI Can Do Without Human Approval

| Action | Tooling | Notes |
|--------|---------|-------|
| Parse requirements from Jira/Confluence | Claude API | Read-only |
| Generate draft LRS, HLD, LLD documents | Claude API | Draft only — not published until approved |
| Generate user stories with ACs | Claude API | Created in Jira as Draft status |
| Generate code scaffolds and test stubs | Copilot / Claude | In feature branch only |
| Run pre-commit checks (lint, secret scan) | Gitleaks, Checkstyle | Blocks commit locally |
| Open pull requests | GitHub API | Requires human review to merge |
| Run CI pipeline stages (build, test, scan) | Jenkins / Harness | Automated but results require gate pass |
| Deploy to DEV and SIT environments | Helm / kubectl | Auto-deploy on pipeline pass |
| Generate test reports | JaCoCo, SonarQube, Trivy | Read and publish only |
| Send Slack / email notifications | Slack API | Informational only |

### 🚫 What Requires Human Approval (AI Cannot Do Alone)

| Action | Reason |
|--------|--------|
| Lock LRS document | Binding commitment — requires Architect + PO sign-off |
| Merge PR to main/release branch | Code quality and security accountability |
| Promote to UAT environment | Business stakeholder acceptance required |
| Promote to PROD environment | Change Advisory Board approval required |
| Modify infrastructure-as-code (Terraform) | Infrastructure risk — separate IaC review process |
| Apply DB schema migrations in production | Data integrity risk |
| Modify pipeline definitions (Jenkinsfile/Harness) | Pipeline integrity — requires senior engineer review |
| Grant or revoke access permissions | Security — IAM / AD process |
| Suppress a SonarQube or CVE finding | Must be documented with justification and expiry date |

---

## 4. Data Classification & AI Usage Policy

| Data Classification | Can Be Sent to External AI API? | Approved AI Tools | Notes |
|--------------------|--------------------------------|------------------|-------|
| PUBLIC | ✅ Yes | Claude, Copilot | No restrictions |
| INTERNAL | ✅ Yes (anonymised) | Claude, Copilot | Remove company-specific names before sending |
| CONFIDENTIAL | ⚠️ On-prem AI only | Private Claude instance | Must not leave company network |
| RESTRICTED / PII | ❌ Never | None | Synthetic test data only — never real data |
| PCI / Financial Data | ❌ Never | None | Strict regulatory prohibition |

> ⚠️ **Enforcement:** Engineers must complete AI Data Handling Training before using AI tooling on INTERNAL or higher classified projects. Violations reported to Security Operations.

---

## 5. Audit Trail Requirements

### What Must Be Logged

| Event | Logged In | Retained For |
|-------|-----------|-------------|
| LRS approved | Git commit + Confluence | 7 years |
| HLD / LLD approved | Git commit + Confluence | 7 years |
| Story created by AI | Jira audit log | 3 years |
| PR opened / merged | GitHub / Bitbucket | 3 years |
| Pipeline run | Jenkins / Harness | 1 year |
| SonarQube quality gate result | SonarQube + Artifactory | 1 year |
| Container scan result | Artifactory | 1 year |
| Deployment event | Kubernetes audit log + Splunk | 3 years |
| UAT sign-off | Jira ticket + Confluence | 7 years |
| CAB approval | ServiceNow / Jira | 7 years |
| Security exception / suppression | Jira + Security register | 7 years |

### Traceability Matrix

Every release must include a completed Traceability Matrix showing:

```
LRS-ID → HLD Section → LLD Section → Story ID → Code PR → Test Case → Test Result → Deployment Version
```

The traceability matrix is auto-generated by the framework from Jira labels, Git commits, and test reports.

---

## 6. Quality Gates Summary

### Pipeline Quality Gates (Hard Stops)

| Gate | Threshold | Action on Breach |
|------|-----------|-----------------|
| Unit test pass rate | 100% | Pipeline FAIL — blocked |
| Code coverage | ≥ 85% line | Pipeline FAIL — blocked |
| SonarQube Quality Gate | Passed | Pipeline FAIL — blocked |
| SonarQube blocker issues | 0 | Pipeline FAIL — blocked |
| SonarQube critical issues | 0 | Pipeline FAIL — blocked |
| OWASP dependency CVE (HIGH/CRITICAL) | 0 | Pipeline FAIL — blocked |
| Gitleaks secret detection | 0 | Pipeline FAIL — blocked |
| Trivy image CVE (HIGH/CRITICAL) | 0 | Pipeline FAIL — blocked |
| Integration test pass rate | 100% | Pipeline FAIL — blocked |
| Smoke test pass rate | 100% | Deployment ROLLED BACK |
| Canary error rate | < 1% | Auto-rollback triggered |
| Canary P95 latency | < 500ms | Auto-rollback triggered |

### Exception Process

If a quality gate exception is required (e.g., suppressing a false-positive CVE):

1. Engineer raises exception request in Jira with full justification
2. Security Engineer reviews and approves/rejects within 2 business days
3. Exception is documented with: reason, risk assessment, expiry date, compensating controls
4. Exception is recorded in the Security Exception Register
5. Exception is reviewed at next security review cycle

---

## 7. DORA Metrics Targets

> Track and report monthly. Target baselines for a high-performing team.

| Metric | Definition | Target |
|--------|-----------|--------|
| Deployment Frequency | How often we deploy to production | Multiple times per week |
| Lead Time for Changes | Time from commit to production | < 1 week |
| Change Failure Rate | % of deployments causing incidents | < 5% |
| MTTR | Time to restore service after incident | < 1 hour |

---

## 8. AI Model Version Governance

All AI-generated artifacts must record:

```yaml
ai_metadata:
  model: claude-3-5-sonnet-20241022
  prompt_version: "1.2.0"
  generated_at: "2024-01-15T10:30:00Z"
  generated_by: "engineer@company.com"
  reviewed_by: "architect@company.com"
  review_date: "2024-01-16"
```

This metadata is stored in the document header and in Git commit message.

---

## 9. Incident Response for AI-Generated Code

If a production incident is traced to AI-generated code:

1. **Immediate:** Rollback to previous stable version
2. **Within 1 hour:** Root cause investigation — identify which AI-generated component failed
3. **Within 24 hours:** Incident report filed in ServiceNow
4. **Within 5 days:** Post-incident review — update AI prompt templates and review checklists
5. **Learning:** Share lessons in team retrospective and update this governance document

---

*Approved by: Platform Engineering Leadership*
*Next review: Quarterly*
