---
name: generate-unit-tests
version: 1.0.0
phase: "05 — Automated Testing"
description: >
  Generates comprehensive JUnit 5 unit tests for Spring Boot service classes,
  covering all acceptance criteria scenarios with Mockito, AssertJ, and
  parameterized tests. Use this skill whenever a user wants to write unit tests,
  increase test coverage, generate test cases from ACs, test a service class,
  mock dependencies, or write JUnit tests. Trigger when the user says "write
  unit tests", "generate tests for this class", "test this service", "improve
  test coverage", "write test cases from ACs", "mock this", or "I need tests
  for this". Always generate tests from the service class AND the acceptance
  criteria — tests without AC traceability are incomplete.
inputs:
  - name: source_class
    description: Full Java source code of the class to test (service, controller, or utility)
    required: true
  - name: acceptance_criteria
    description: Given/When/Then AC scenarios from the Jira story
    required: true
  - name: lrs_ref
    description: "LRS requirement ID, e.g. LRS-20240115-FR-01"
    required: false
  - name: coverage_target
    description: "Minimum line coverage target as percentage (default: 85)"
    required: false
    default: "85"
output: Complete JUnit 5 test class with all scenarios, mocks, and coverage annotations
---

# Skill: Generate Unit Tests

## Purpose

Produce a complete, CI-ready JUnit 5 test class that achieves the target coverage, covers every acceptance criteria scenario, and serves as living documentation of the class behaviour.

---

## Step-by-Step Instructions

### Step 1 — Analyse the Source Class

Before writing any test, read the source class and extract:
- All public methods (these all need tests)
- All dependencies (these become Mockito mocks)
- All possible return types and exceptions
- All conditional branches (each branch needs at least one test)
- Any use of `SecurityContextHolder` (requires SecurityContext mock)
- Any use of `MDC` (verify MDC is populated in relevant tests)

### Step 2 — Map ACs to Test Methods

For each Given/When/Then scenario, create one `@Test` method.
Name the test using the pattern: `{method}_{condition}_{expectedOutcome}`

Examples:
```
create_validRequest_shouldPersistAndReturnDto
create_duplicateIdempotencyKey_shouldThrowDuplicateException
create_repositoryThrowsException_shouldPropagateException
getById_existingId_shouldReturnDto
getById_nonExistentId_shouldThrowNotFoundException
getById_softDeletedEntity_shouldThrowNotFoundException
```

### Step 3 — Write the Test Class Structure

```java
// File: src/test/java/{package}/service/{ClassName}Test.java
// LRS Reference: {lrs_ref}
// Coverage Target: {coverage_target}%

package {package}.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("{ClassName} — Unit Tests")
class {ClassName}Test {

    // ── MOCKS ─────────────────────────────────────────────────
    @Mock private {Dependency1} dependency1;
    @Mock private {Dependency2} dependency2;
    @InjectMocks private {ClassName} subject;

    // ── FIXTURES ──────────────────────────────────────────────
    private static final UUID TEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    // Centralise test data builders — do not duplicate inline
    private {EntityName}RequestDto buildValidRequest() {
        return {EntityName}RequestDto.builder()
            .reference("TEST-REF-001")
            // add all required fields
            .build();
    }

    private {EntityName} buildEntity() {
        return {EntityName}.builder()
            .id(TEST_ID)
            // add fields
            .status({EntityName}Status.PENDING)
            .build();
    }

    private {EntityName}ResponseDto buildResponseDto() {
        return {EntityName}ResponseDto.builder()
            .id(TEST_ID)
            // add fields
            .build();
    }

    // ── NESTED TEST GROUPS ────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {
        // Tests for create method
    }

    @Nested
    @DisplayName("getById()")
    class GetById {
        // Tests for getById method
    }
}
```

### Step 4 — Write Each Test Method

Follow this pattern for every test:

```java
@Test
@DisplayName("{business language description of what this tests}")
void {method}_{condition}_{expectedOutcome}() {
    // ── ARRANGE ───────────────────────────────────────────────
    var request = buildValidRequest();
    var entity  = buildEntity();
    var dto     = buildResponseDto();

    given(dependency1.method(any())).willReturn(entity);
    given(dependency2.method(entity)).willReturn(dto);

    // ── ACT ───────────────────────────────────────────────────
    var result = subject.create(request);

    // ── ASSERT ────────────────────────────────────────────────
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(TEST_ID);

    // Verify interactions
    then(dependency1).should().method(any());
    then(dependency2).should().method(entity);
    then(dependency1).shouldHaveNoMoreInteractions();
}
```

### Step 5 — Cover All Scenario Types

Generate a test for each of these categories (as applicable):

**Happy Path Tests:**
- Valid input → correct output → correct interactions verified
- Verify that all dependencies were called with correct arguments
- Verify return value content (not just non-null)

**Input Validation Tests:**
- Null input → NullPointerException or custom exception
- Empty/blank strings → validation exception
- Negative numbers → validation exception
- Boundary values (min/max)

**Exception / Error Path Tests:**
- Repository throws `DataAccessException` → verify behaviour
- Mapper returns null → verify behaviour
- External service unavailable → verify fallback or exception
- Each custom exception type has its own test

**State Tests:**
- Verify entity state is set correctly before save (status, createdBy, etc.)
- Verify `@Transactional` boundary respected (use `@Transactional` on test if needed)
- Verify soft delete logic (deletedAt set, not physical delete)

**Idempotency Tests (if applicable):**
- Duplicate idempotency key → throws DuplicateException, verify no save called
- First request → saves and publishes
- Second request with same key → returns existing, no save

**Security Context Tests (if applicable):**
- Authenticated user → createdBy set to username
- No authentication → createdBy set to "system"

**Parameterized Tests (for data-driven scenarios):**
```java
@ParameterizedTest
@ValueSource(strings = {"", " ", "   "})
@DisplayName("create: should throw validation exception when reference is blank")
void create_blankReference_shouldThrowValidationException(String reference) {
    var request = buildValidRequest().toBuilder().reference(reference).build();
    assertThatThrownBy(() -> subject.create(request))
        .isInstanceOf(ConstraintViolationException.class);
}
```

**Null Parameter Tests:**
```java
@Test
@DisplayName("create: should throw NullPointerException when request is null")
void create_nullRequest_shouldThrowNullPointerException() {
    assertThatThrownBy(() -> subject.create(null))
        .isInstanceOf(NullPointerException.class);
    then(repository).shouldHaveNoInteractions();
}
```

### Step 6 — Coverage Verification Comment

Add a comment block after the class listing all methods and their coverage:

```java
/*
 * ── COVERAGE SUMMARY ─────────────────────────────────────────
 * Method                | Tests | Branches Covered
 * create()              |  5    | happy path, null input, dup key, repo throws, mapper null
 * getById()             |  3    | found, not found, soft deleted
 * getCurrentUserId()    |  2    | authenticated, unauthenticated
 * Expected coverage: ~{coverage_target}%
 * Run: mvn test -Dtest={ClassName}Test jacoco:report
 * ─────────────────────────────────────────────────────────────
 */
```

---

## Quality Checklist

- [ ] Every public method has at least one test
- [ ] Every AC scenario from the Jira story maps to a named test
- [ ] All tests follow AAA (Arrange/Act/Assert) with clear section comments
- [ ] All assertions use AssertJ (not JUnit `assertEquals`)
- [ ] All mock interactions verified with `then().should()` (not just asserting return values)
- [ ] No test uses `@Autowired` — pure unit tests with `@ExtendWith(MockitoExtension.class)`
- [ ] No test uses `Thread.sleep()` — timing issues resolved with Mockito
- [ ] Test display names are business-readable (not method names)
- [ ] Parameterized tests used for data-driven scenarios
- [ ] Test data builders are in private methods (not duplicated inline)
- [ ] No test modifies static state

---

## Rules

- **Never use `assertTrue(result != null)`** — use `assertThat(result).isNotNull()`
- **Never ignore the `verify` step** — always verify mock interactions for state-changing methods
- **Never test framework behaviour** — don't test that Spring DI works; test business logic
- **Never leave TODOs in test code** — if you cannot test something, say why in a comment
- **One assertion concept per test** — split if testing multiple unrelated things
- **Use `BDDMockito`** (`given/then`) style not `Mockito` (`when/verify`) style for readability
