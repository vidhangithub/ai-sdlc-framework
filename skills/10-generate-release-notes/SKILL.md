---
name: generate-release-notes
version: 1.0.0
phase: "07 — Deployment"
description: >
  Generates structured, audience-appropriate release notes from Git commit
  history, merged PR titles, and Jira story summaries. Use this skill whenever
  a user wants to write release notes, produce a changelog, document what changed
  in a release, summarise a sprint, or communicate changes to stakeholders.
  Trigger when the user says "write release notes", "generate changelog",
  "what changed in this release", "produce release documentation", "summarise
  this sprint's changes", "create the release notes for stakeholders", or
  "document this release". Always produce multiple audience versions — technical
  and business-readable — in a single run.
inputs:
  - name: git_log
    description: Output of `git log --oneline --no-merges v{prev}..HEAD` or list of PR titles
    required: true
  - name: jira_stories
    description: List of completed Jira stories with titles and LRS refs (optional)
    required: false
  - name: service_name
    description: Service or product name
    required: true
  - name: version
    description: "Release version, e.g. 1.2.0"
    required: true
  - name: prev_version
    description: "Previous release version, e.g. 1.1.0"
    required: false
  - name: release_date
    description: "Planned release date, e.g. 2024-02-15"
    required: true
output: Release notes in three formats — Technical, Business, and Confluence-ready Markdown
---

# Skill: Generate Release Notes

## Purpose

Produce clear, accurate, and audience-appropriate release notes from raw Git/Jira data. Every release needs three versions: one for engineers (technical), one for business stakeholders (plain language), and one for the Confluence release page (structured).

---

## Step-by-Step Instructions

### Step 1 — Parse and Categorise Changes

Read all Git commits / PR titles / Jira stories and classify each into a category:

| Category | Commit Prefix / Signal | Description |
|---------|----------------------|-------------|
| `NEW_FEATURE` | `feat:`, story with no LRS bug ref | New functionality delivered |
| `BUG_FIX` | `fix:`, `bugfix/` branch | Defect remediation |
| `PERFORMANCE` | `perf:`, performance story | Response time or throughput improvement |
| `SECURITY` | `sec:`, `security`, CVE reference | Security fix or hardening |
| `BREAKING_CHANGE` | `feat!:`, `BREAKING CHANGE:` in commit | API or contract change requiring consumer action |
| `DEPENDENCY_UPDATE` | `chore(deps):`, Dependabot | Library version bumps |
| `INFRASTRUCTURE` | `chore:`, `ci:` | Pipeline, config, or deployment changes |
| `DOCUMENTATION` | `docs:` | Documentation only |

**Important:** If a commit touches security (CVE fix, auth change, encryption change) — always flag it as `SECURITY` even if the commit message says `fix`.

### Step 2 — Identify Breaking Changes

Scan all changes for breaking changes:
- API contract changes (removed/renamed endpoints, changed response shapes)
- Database schema changes requiring migration
- Configuration property renames or removals
- New mandatory headers or auth scope requirements

**Breaking changes must be highlighted prominently in all three output formats.**

### Step 3 — Link to Jira Stories

If Jira story list is provided, enrich each change with:
- Jira story ID (hyperlinked)
- LRS traceability ID
- Story point count (shows release scope)

### Step 4 — Produce Technical Release Notes

For the engineering team and DevOps:

```markdown
# Release Notes — {service_name} v{version}

**Release Date:** {release_date}
**Previous Version:** {prev_version}
**Deployment Strategy:** Canary / Blue-Green
**Rollback Version:** {prev_version}

---

## ⚠️ Breaking Changes

> Action required before deploying downstream consumers.

| Change | Impact | Consumer Action Required |
|--------|--------|------------------------|
| {description} | {who is affected} | {what consumers must do} |

---

## 🆕 New Features

| Jira | LRS Ref | Description | Notes |
|------|---------|-------------|-------|
| {PROJ-nn} | LRS-{date}-FR-{nn} | {technical description} | |

---

## 🐛 Bug Fixes

| Jira | Description | Root Cause |
|------|-------------|-----------|
| {PROJ-nn} | {description} | {brief root cause} |

---

## 🔒 Security

| CVE / Reference | Severity | Description | Remediation |
|----------------|----------|-------------|-------------|
| {CVE-xxx / PROJ-nn} | HIGH/MED/LOW | {description} | {what was done} |

---

## ⚡ Performance Improvements

| Area | Before | After | Method |
|------|--------|-------|--------|
| {endpoint} | P95: {n}ms | P95: {n}ms | {caching / query optimisation / etc} |

---

## 📦 Dependency Updates

| Library | From | To | CVEs Fixed |
|---------|------|----|-----------|
| spring-boot | {old} | {new} | {list or 'None'} |

---

## 🔧 Infrastructure / Config Changes

| Change | Impact | Action Required |
|--------|--------|----------------|
| {description} | {dev/ops impact} | {update config / pipeline step} |

---

## Deployment Checklist

- [ ] DB migrations reviewed and approved (if applicable)
- [ ] Configuration changes applied to all environments
- [ ] Downstream consumer teams notified of breaking changes
- [ ] Rollback plan confirmed: `helm rollback {release} 1`
- [ ] Smoke tests pass in target environment
- [ ] Monitoring dashboards reviewed post-deployment
- [ ] CAB change record raised: CR-{number}

---

## Testing Evidence

| Test Type | Result | Report Link |
|-----------|--------|------------|
| Unit Tests | {n} passed, 0 failed | {link} |
| Integration Tests | {n} passed, 0 failed | {link} |
| SonarQube Gate | Passed | {link} |
| Trivy Image Scan | 0 HIGH/CRITICAL | {link} |
| Performance Tests | P95: {n}ms (SLA: {n}ms) ✅ | {link} |
| UAT Sign-off | Approved by {name} on {date} | {link} |

---

## Git Log Summary

```
{paste git log --oneline here}
```
```

### Step 5 — Produce Business Release Notes

For Product Owners, business stakeholders, and CAB:

```markdown
# {Service Display Name} — Release {version}

**Release Date:** {release_date}
**Prepared by:** {team name}

---

## Summary

{2-3 sentences summarising the business value of this release in plain English.
No technical jargon. Focus on what changed for users and why it matters.}

---

## What's New

{For each new feature — one bullet in plain English}
- Users can now {describe capability in business terms} — {why this matters}
- {Feature 2}

---

## What's Fixed

{For each bug fix — one bullet in plain English}
- Resolved an issue where {describe problem in user terms}

---

## Important: Action Required

{Only present if there are breaking changes or consumer actions needed}

> ⚠️ **Attention:** The following changes require action from consuming teams:
> - {Team/system}: {what they need to do, by when}

---

## Security & Compliance

{Only if security changes present}
- {Brief plain-language description of security improvement}
- Compliance: {GDPR / PCI / SOX impact — none / maintained / improved}

---

## Rollback Plan

If issues are detected after deployment, the previous version ({prev_version}) can be restored within 15 minutes by the operations team.

---

*For technical details, see the Technical Release Notes.*
*For questions, contact: {team-slack-channel}*
```

### Step 6 — Produce Confluence Page Structure

```markdown
# Release: {service_name} v{version} — {release_date}

## Status: PLANNED / IN PROGRESS / DEPLOYED / ROLLED BACK

| Field | Value |
|-------|-------|
| Service | {service_name} |
| Version | {version} |
| Previous | {prev_version} |
| Release Date | {release_date} |
| Release Manager | {name} |
| CAB Reference | CR-{number} |
| Jira Release | {link to Jira Fix Version} |

---

## Release Contents

{embed or link the Business Release Notes here}

---

## Technical Details

{embed or link the Technical Release Notes here}

---

## Approvals

| Role | Name | Approved | Date |
|------|------|---------|------|
| Engineering Lead | | ☐ | |
| Product Owner | | ☐ | |
| Security | | ☐ (if security changes) | |
| CAB | | ☐ | |

---

## Post-Deployment Verification

| Check | Status | Timestamp | Notes |
|-------|--------|-----------|-------|
| Deployment completed | | | |
| Smoke tests passed | | | |
| Monitoring normal | | | |
| No incidents raised | | | |
| Release closed in Jira | | | |
```

---

## Quality Checklist

- [ ] All three formats produced (Technical, Business, Confluence)
- [ ] Breaking changes prominently highlighted in all formats
- [ ] Security changes listed even if minor
- [ ] Jira story IDs included (if provided)
- [ ] LRS traceability IDs included where available
- [ ] Deployment checklist complete with rollback plan
- [ ] Testing evidence table complete
- [ ] Business format contains NO technical jargon
- [ ] Version numbers consistent throughout
- [ ] All changes categorised (nothing left uncategorised)

---

## Rules

- **Breaking changes must appear first** in all formats — never bury them
- **Never use technical jargon in the Business format** — translate everything
- **Security changes are never optional** — always list them, even if low severity
- **Every release must include a rollback plan** — non-negotiable
- **If a Jira story is not in the git log, flag it** as potentially missing from the release
- **Never mark a release as complete** until the post-deployment verification table is filled
