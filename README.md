# AI-Driven SDLC Framework

> End-to-end AI-augmented software delivery — from requirement intake to production deployment on Amazon EKS and OpenShift OCP.

---

## Overview

This repository contains the full framework, templates, pipeline definitions, and deployment configurations for an AI-assisted Software Development Lifecycle (SDLC). Every phase produces a versioned, traceable artifact.

```
Requirement → Design → Stories → Code → Tests → Pipeline → Deploy → Govern
```

---

## Repository Structure

```
ai-sdlc-framework/
├── 01-requirement-intake/       # LRS templates, clarification logs
├── 02-design-documents/         # HLD and LLD templates
├── 03-story-creation/           # Story templates, Jira automation scripts
├── 04-code-development/         # Coding standards, Checkstyle, AI prompts
├── 05-automated-testing/        # Test strategy, Gatling, ZAP config
├── 06-cicd-pipeline/            # Jenkinsfile, Harness pipeline YAML
├── 07-deployment/               # Helm charts (EKS + OCP), Argo Rollouts
├── 08-governance/               # Approval gates, audit trail policy
└── docs/                        # Interactive HTML framework document
```

---

## Phases

| # | Phase | Key Output | Gate |
|---|-------|-----------|------|
| 01 | Requirement Intake | Locked Requirement Spec (LRS) | Architect + PO approval |
| 02 | Design Documents | HLD + LLD (versioned) | Principal Architect approval |
| 03 | Story Creation | Sprint-ready Jira backlog | Product Owner approval |
| 04 | Code Development | PR with unit tests | Senior Engineer review |
| 05 | Automated Testing | Full test suite results | All gates green |
| 06 | CI/CD Pipeline | Build + scan + publish | Quality gate pass |
| 07 | Deployment | Running service on EKS/OCP | Smoke tests + CAB |
| 08 | Governance | Audit trail, traceability matrix | Compliance sign-off |

---

## Quick Start

### Prerequisites
- Java 17+, Maven 3.9+
- Docker Desktop
- kubectl + Helm 3
- Access to Jenkins/Harness instance
- GitHub PAT with `repo` scope

### Using This Framework

1. **Start with Phase 01** — copy `01-requirement-intake/LRS-template.md` and fill in your requirement
2. **Run AI design generation** using prompts in `02-design-documents/`
3. **Generate stories** using `03-story-creation/jira-api-script.py`
4. **Bootstrap your service** using the code scaffold prompts in `04-code-development/`
5. **Copy the Jenkinsfile** from `06-cicd-pipeline/` into your service repo
6. **Deploy using Helm** charts from `07-deployment/helm/`

---

## Key Principles

- **AI accelerates, humans approve** — every critical decision has a human gate
- **Traceability first** — LRS requirement IDs thread through every artifact
- **Security by default** — SAST, DAST, CVE scanning built into every pipeline
- **No secrets in code** — HashiCorp Vault / AWS Secrets Manager enforced
- **Zero critical findings policy** — pipeline blocks on HIGH/CRITICAL vulnerabilities

---

## Technology Stack

| Layer | Tools |
|-------|-------|
| AI Generation | Claude, GitHub Copilot |
| Language | Java 17, Spring Boot 3.x |
| Build | Maven, Docker |
| SAST | SonarQube, SpotBugs, Checkstyle |
| Testing | JUnit 5, Testcontainers, Gatling, OWASP ZAP |
| CI/CD | Jenkins, Harness |
| Container Platform | Amazon EKS, OpenShift OCP |
| Observability | Prometheus, Grafana, Jaeger, Loki |
| Secrets | HashiCorp Vault, AWS Secrets Manager |

---

## Contributing

All changes must follow the branching strategy: `feature/LRS-{id}-{description}` → PR → peer review → merge to `main`.

---

*Maintained by the Platform Engineering team. Version 1.0.0*
