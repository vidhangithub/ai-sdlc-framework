---
name: review-code-security
version: 1.0.0
phase: "04 — Code Development"
description: >
  Performs a thorough OWASP Top 10 and Java-specific security review of Spring
  Boot code, producing a prioritised list of findings with remediation code.
  Use this skill whenever a user wants a security review, security audit, OWASP
  check, vulnerability assessment, or security sign-off on Java code. Trigger
  when the user says "review this for security", "check for vulnerabilities",
  "security audit this code", "OWASP review", "is this code secure", "check for
  injection risks", "security review before PR", or "flag any security issues".
  This skill should be run on every PR before merge — it is not optional. Output
  always includes severity ratings and concrete fix examples.
inputs:
  - name: source_code
    description: Java source code to review (paste one or more class files)
    required: true
  - name: context
    description: "Brief description of what this code does and who calls it (optional)"
    required: false
  - name: data_classification
    description: "Data classification: PUBLIC | INTERNAL | CONFIDENTIAL | RESTRICTED"
    required: false
    default: "INTERNAL"
output: Prioritised security findings report with OWASP references and fix examples
---

# Skill: Security Code Review

## Purpose

Identify security vulnerabilities in Spring Boot Java code before they reach production. Every finding includes severity, OWASP reference, affected code location, explanation, and a concrete remediation code example.

---

## Review Process

Work through each category below systematically. Do not skip categories even if the code appears clean.

---

### Category 1 — Injection Vulnerabilities (OWASP A03)

Check for:

**SQL Injection:**
- Native queries built with string concatenation → CRITICAL
- JPQL with string interpolation → HIGH
- Named parameters used correctly → PASS

```java
// ❌ CRITICAL — SQL Injection
entityManager.createNativeQuery("SELECT * FROM payments WHERE id = " + id);

// ✅ Fix
entityManager.createNativeQuery("SELECT * FROM payments WHERE id = :id")
    .setParameter("id", id);
```

**LDAP/XML/XPath Injection:**
- Any LDAP queries built with user input → HIGH
- XML parsing of user input without validation → HIGH

**Log Injection:**
- User-controlled data logged without sanitisation (can inject fake log entries) → MEDIUM
```java
// ❌ MEDIUM — Log Injection
log.info("User searched for: {}", userInput);  // if userInput contains \n

// ✅ Fix — encode or sanitise before logging
log.info("User searched for: {}", userInput.replaceAll("[\n\r]", "_"));
```

---

### Category 2 — Broken Access Control (OWASP A01)

Check for:
- Missing `@PreAuthorize` on controller methods → HIGH
- Horizontal privilege escalation (user A can access user B's data) → CRITICAL
- Direct object references without ownership check → HIGH
- Admin endpoints accessible by regular users → CRITICAL
- Missing `@Secured` or role checks in service layer for sensitive operations → HIGH

```java
// ❌ HIGH — No access control
@GetMapping("/{id}")
public PaymentDto getPayment(@PathVariable UUID id) {
    return service.getById(id);  // anyone authenticated can get any payment
}

// ✅ Fix — verify ownership
@GetMapping("/{id}")
@PreAuthorize("hasAuthority('SCOPE_read')")
public PaymentDto getPayment(@PathVariable UUID id,
                              Authentication auth) {
    return service.getByIdForUser(id, auth.getName()); // service checks ownership
}
```

---

### Category 3 — Cryptographic Failures (OWASP A02)

Check for:
- Weak algorithms: MD5, SHA-1, DES, 3DES → CRITICAL
- Hardcoded encryption keys → CRITICAL
- Sensitive data stored in plaintext → CRITICAL
- Sensitive data logged → HIGH
- PII in exception messages → MEDIUM
- Insufficient entropy in random number generation (`Math.random()` for security purposes) → HIGH

```java
// ❌ CRITICAL — Weak hash
MessageDigest.getInstance("MD5")

// ✅ Fix
MessageDigest.getInstance("SHA-256")

// ❌ HIGH — Logging sensitive data
log.info("Processing card: {}", cardNumber);

// ✅ Fix — mask sensitive data
log.info("Processing card ending: {}", maskCard(cardNumber));
```

---

### Category 4 — Security Misconfiguration (OWASP A05)

Check for:
- CSRF disabled without justification (stateless REST APIs are OK to disable) → NOTE if disabled
- CORS configured with `*` wildcard → HIGH in production
- Actuator endpoints exposed without auth → HIGH
- HTTP (not HTTPS) in any hardcoded URL → HIGH
- Debug logging enabled in production config → MEDIUM
- Default credentials in config files → CRITICAL
- Stack traces exposed in API responses → MEDIUM

```java
// ❌ HIGH — Wildcard CORS
.cors(cors -> cors.configurationSource(request -> {
    var config = new CorsConfiguration();
    config.addAllowedOrigin("*");  // Never in production
}))

// ✅ Fix — explicit allowed origins
config.setAllowedOrigins(List.of("https://app.company.com", "https://admin.company.com"));
```

---

### Category 5 — Vulnerable Dependencies (OWASP A06)

Note: Full dependency scanning is done by OWASP Dependency-Check in the pipeline. During code review, flag:
- Dependencies imported via reflection or dynamic classloading without version pinning → MEDIUM
- Outdated Spring Boot version (check against current LTS) → HIGH
- Known vulnerable library versions if recognisable → HIGH

---

### Category 6 — Identification and Authentication Failures (OWASP A07)

Check for:
- JWT signature not verified (algorithm=none attack) → CRITICAL
- Weak JWT secret (hardcoded, short, dictionary word) → CRITICAL
- Missing token expiry validation → HIGH
- Session fixation vulnerabilities → HIGH (should not apply to stateless REST)
- No brute force protection on auth endpoints → MEDIUM

---

### Category 7 — Data Integrity Failures (OWASP A08)

Check for:
- Deserialization of untrusted data (Java ObjectInputStream) → CRITICAL
- YAML/XML loaded from user input without safe parsers → HIGH
- Missing `@Version` on JPA entities (potential lost update vulnerability) → MEDIUM

---

### Category 8 — Security Logging Failures (OWASP A09)

Check for:
- Security events not logged (login, logout, access denied, privilege escalation attempt) → HIGH
- Log entries missing correlation IDs (traceId) → MEDIUM
- Sensitive data in logs (passwords, tokens, card numbers) → HIGH
- Logs not including: who, what, when, outcome → MEDIUM

---

### Category 9 — Server-Side Request Forgery (OWASP A10)

Check for:
- HTTP client calls with URLs from user input → HIGH
- URL redirect with user-controlled destination → HIGH
- File reads from paths constructed using user input → HIGH

```java
// ❌ HIGH — SSRF
String url = request.getParameter("callbackUrl");
restTemplate.getForObject(url, String.class);  // user controls the URL

// ✅ Fix — allowlist approach
if (!ALLOWED_HOSTS.contains(URI.create(url).getHost())) {
    throw new SecurityException("Disallowed callback URL host");
}
```

---

### Category 10 — Java/Spring Specific Checks

Additional checks beyond OWASP Top 10:

**Hardcoded Secrets:**
```java
// ❌ CRITICAL
private static final String API_KEY = "abc123secret";
String password = "P@ssw0rd123";
```

**Mass Assignment:**
```java
// ❌ HIGH — User can set any field including ID, status, createdBy
@PatchMapping
public void update(@RequestBody PaymentEntity entity) { // Entity, not DTO!
    repository.save(entity);
}
// ✅ Fix — always accept DTOs, never entities
```

**Open Redirect:**
```java
// ❌ MEDIUM
return "redirect:" + request.getParameter("next");
// ✅ Fix — validate against allowlist
```

**Exception Information Leakage:**
```java
// ❌ MEDIUM — Stack trace to client
return ResponseEntity.status(500).body(e.getStackTrace().toString());
// ✅ Fix — use GlobalExceptionHandler with safe ProblemDetail
```

---

## Output Format

Produce findings in this format:

```markdown
## Security Review Report

**Service:** {service-name}
**Reviewer:** AI (review-code-security skill v1.0.0)
**Date:** {today}
**Data Classification:** {classification}
**Overall Risk:** CRITICAL / HIGH / MEDIUM / LOW / PASS

---

### CRITICAL Findings (Must fix before merge)

#### [CRIT-01] {Finding Title}
- **OWASP Category:** A{nn} — {category name}
- **Location:** `{ClassName}.java` line ~{n}
- **Description:** {What the vulnerability is and why it's dangerous}
- **Affected Code:**
  ```java
  {vulnerable code snippet}
  ```
- **Remediation:**
  ```java
  {fixed code}
  ```

---

### HIGH Findings (Must fix before release)

#### [HIGH-01] {Finding Title}
...

---

### MEDIUM Findings (Fix in next sprint)

#### [MED-01] {Finding Title}
...

---

### LOW / Informational

#### [LOW-01] {Finding Title}
...

---

### Security Checklist Summary

| Check | Result | Notes |
|-------|--------|-------|
| SQL Injection (A03) | ✅ PASS / ❌ FAIL | |
| Access Control (A01) | | |
| Cryptographic Failures (A02) | | |
| Security Misconfiguration (A05) | | |
| Authentication Failures (A07) | | |
| Data Integrity (A08) | | |
| Security Logging (A09) | | |
| SSRF (A10) | | |
| Hardcoded Secrets | | |
| Mass Assignment | | |

**Merge Recommendation:** 
- ❌ BLOCK — Critical or High findings present. Must remediate before merge.
- ⚠️ CONDITIONAL — Medium findings only. Must create tickets before merge.
- ✅ APPROVE — No significant findings. Document Low findings in backlog.
```

---

## Quality Rules for This Skill

- **Never approve code with CRITICAL findings** — always block
- **Always include a remediation code example** — never just describe the fix in words
- **Always check all 10 categories** — do not skip any even if code seems clean
- **If no findings in a category, write `✅ No findings`** — never leave categories blank
- **Never report false positives as findings** — if unsure, mark as `[REVIEW MANUALLY]` with explanation
