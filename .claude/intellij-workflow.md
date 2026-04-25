# Using Claude in IntelliJ — Workflow Guide

> Practical guide for engineers using the Claude IntelliJ plugin with this repo.

---

## Initial Setup (Do Once)

### 1. Pin the Project Context

Every time you start a new Claude session in IntelliJ:

1. Open `.claude/project.md`
2. `Ctrl+A` to select all
3. Paste into the Claude chat panel
4. Claude now knows your full project context

**Tip:** Add `.claude/project.md` to your IntelliJ favourites tab so it's one click away.

### 2. Create a Custom Scope in IntelliJ

Go to `Settings → Appearance & Behaviour → Scopes → Add`:
- Name: `AI Skills`
- Pattern: `file:skills//*.md`

This lets you quickly search and open skill files via `Ctrl+Shift+F` within the skills scope.

---

## Daily Workflows

### Workflow A: Starting a New Feature

You have a Jira Epic URL or description. You want to go from Epic → LRS → Design → Stories.

**Step 1 — Parse the requirement:**
```
1. Open Claude panel in IntelliJ
2. Paste: .claude/project.md
3. Paste: skills/01-parse-requirements/SKILL.md
4. Type: "Here is my Jira Epic: [paste epic description]"
5. Claude produces a draft LRS
6. Save it to: 01-requirement-intake/LRS-{date}-{service}.md
```

**Step 2 — Generate HLD:**
```
1. New Claude session
2. Paste: .claude/project.md
3. Paste: skills/02-generate-hld/SKILL.md
4. Paste: the LRS you just created
5. Claude produces the HLD
6. Save to: 02-design-documents/HLD-{service}-v1.0.md
```

**Step 3 — Generate Stories:**
```
1. New Claude session
2. Paste: .claude/project.md
3. Paste: skills/04-generate-stories/SKILL.md
4. Paste: LRS + LLD
5. Claude produces full backlog in Markdown + YAML
6. Run: python 03-story-creation/jira-api-script.py --file stories.yaml --dry-run
7. If happy: run without --dry-run to push to Jira
```

---

### Workflow B: Implementing a Story

You have a Jira story assigned to you. You want to implement it.

**Step 1 — Generate scaffold:**
```
1. Open Claude panel (Ctrl+Shift+A → "Claude")
2. Paste: .claude/project.md
3. Paste: skills/05-generate-code-scaffold/SKILL.md
4. Tell Claude:
   "Service: payment-service
    Package: com.company.payments
    Entity: Payment
    Features: rest_api, jpa, ibm_mq, security, flyway
    
    Here is the LLD: [paste relevant LLD sections]"
5. Claude outputs all Java files
6. Create the files in your project (copy-paste or use Claude's "Insert at cursor")
```

**Step 2 — Generate unit tests:**
```
1. Open the generated ServiceImpl class
2. Open Claude panel
3. Paste: skills/06-generate-unit-tests/SKILL.md
4. Say: "Generate unit tests for the open file. Here are the ACs: [paste story ACs]"
5. Claude generates the test class
6. Create src/test/.../ServiceTest.java
```

**Step 3 — Security review before PR:**
```
1. Open your completed controller or service file
2. Open Claude panel
3. Paste: skills/08-review-code-security/SKILL.md
4. Say: "Review the currently open file for security issues.
         Data classification: INTERNAL"
5. Fix any CRITICAL or HIGH findings before raising PR
```

---

### Workflow C: Working on an Existing File

The most common daily use — Claude sees your open file directly.

**Generate tests for the current file:**
```
1. Open PaymentServiceImpl.java
2. Open Claude panel
3. Paste: skills/06-generate-unit-tests/SKILL.md  
4. Say: "Write unit tests for the currently open service class"
```

**Explain/review the current file:**
```
1. Open any Java file
2. Open Claude panel
3. Say: "Review this class against the coding standards in this project.
         Flag any violations."
   (Claude already knows the standards from project.md)
```

**Refactor the current file:**
```
1. Select a method or class
2. Open Claude panel
3. Say: "Refactor the selected code to follow this project's coding standards.
         Use Lombok, constructor injection, and RFC 7807 for errors."
```

---

### Workflow D: Pipeline and Deployment Work

**Adapt the Jenkinsfile for your service:**
```
1. Open 06-cicd-pipeline/Jenkinsfile
2. Paste into Claude with:
   "Adapt this Jenkinsfile for service 'payment-service'.
    ECR registry: 123456789.dkr.ecr.eu-west-1.amazonaws.com
    SonarQube server: sonar-prod
    Slack channel: #payments-team-builds"
```

**Generate Helm values for a new environment:**
```
1. Open 07-deployment/helm/values-eks.yaml
2. Paste into Claude with:
   "Create a values-uat.yaml based on this values-eks.yaml.
    UAT-specific changes:
    - 2 replicas (not 3)
    - Lower resource limits: 512m CPU, 512Mi memory
    - Ingress host: payment-service.uat.company.com"
```

---

## IntelliJ Claude Plugin — Tips and Tricks

### Tip 1: Use "Insert at Cursor"
When Claude generates a Java class, instead of copy-pasting:
- Position your cursor in the right file location
- Click "Insert at cursor" in the Claude response (if your plugin version supports it)

### Tip 2: Multi-file Context
To give Claude context of multiple related files at once:
1. Open File A, copy content
2. Open File B, copy content  
3. Paste both into Claude chat with labels:
```
FILE 1 — PaymentService.java:
[paste]

FILE 2 — PaymentServiceTest.java:
[paste]

Now add the missing test cases for the getById method.
```

### Tip 3: Incremental Generation
Don't ask Claude to generate everything at once for a complex service.
Break it into steps:
```
Session 1: Generate entity + DTOs + mapper
Session 2: Generate service interface + impl  
Session 3: Generate controller
Session 4: Generate tests
```
Each session: paste `project.md` + the relevant skill + the output from the previous session.

### Tip 4: Keep Sessions Focused
One task per Claude session. Don't mix "generate code" and "review for security" in the same session — start fresh, paste project.md again.

### Tip 5: The "Rubber Duck" Use Case
For any existing code you don't understand:
```
1. Open the file
2. Paste project.md
3. Say: "Explain what this class does and how it fits into the overall
         payment-service architecture as described in our framework."
```

---

## File Naming Conventions

| Artifact | Location | Naming |
|---------|---------|--------|
| LRS | `01-requirement-intake/` | `LRS-{YYYYMMDD}-{service}.md` |
| HLD | `02-design-documents/` | `HLD-{service}-v{n}.md` |
| LLD | `02-design-documents/` | `LLD-{service}-v{n}.md` |
| Stories YAML | `03-story-creation/` | `stories-{service}-sprint{n}.yaml` |
| Service code | Your service repo (not this repo) | Standard Maven structure |

---

## What NOT to Do

- ❌ Don't paste sensitive data (passwords, real API keys, PII) into Claude chat
- ❌ Don't skip pasting `project.md` — without it Claude doesn't know your standards
- ❌ Don't accept generated code without reviewing it — Claude produces a starting point
- ❌ Don't use Claude to bypass quality gates — it helps you meet them faster, not skip them
- ❌ Don't paste entire files if only a section is relevant — be precise to get better output
