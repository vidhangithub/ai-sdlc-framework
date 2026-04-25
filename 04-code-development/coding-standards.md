# Coding Standards — AI-SDLC Framework

> These standards apply to all Java/Spring Boot services developed under this framework.
> AI code generation prompts reference this document to ensure consistent output.

---

## 1. Java & Spring Boot Standards

### Language & Framework
- **Java:** 17 LTS (use records, sealed classes, text blocks where appropriate)
- **Spring Boot:** 3.x (Jakarta EE namespace — `jakarta.*` not `javax.*`)
- **Build:** Maven with company parent POM

### Annotation Usage
```java
// ✓ CORRECT — use Lombok to eliminate boilerplate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    @NotBlank(message = "Reference must not be blank")
    private String reference;

    @NotNull @Positive
    private BigDecimal amount;

    @NotBlank
    @Size(max = 3)
    private String currency;
}

// ✗ AVOID — manual getters/setters unless Lombok conflicts
```

### Controller Standards
```java
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponseDto create(@Valid @RequestBody PaymentRequestDto request) {
        log.info("Creating payment: reference={}", request.getReference());
        return paymentService.create(request);
    }

    @GetMapping("/{id}")
    public PaymentResponseDto getById(@PathVariable UUID id) {
        return paymentService.getById(id);
    }
}
```

### Service Standards
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final PaymentEventPublisher eventPublisher;

    @Override
    @Transactional
    public PaymentResponseDto create(PaymentRequestDto request) {
        // 1. Check idempotency
        // 2. Map DTO → Entity
        // 3. Persist
        // 4. Publish event
        // 5. Map Entity → Response DTO
        // 6. Return
    }
}
```

---

## 2. Package Naming Convention

```
com.{company}.{domain}.{service}/
├── config/
├── controller/
├── service/
│   ├── {Name}Service.java          (interface)
│   └── impl/{Name}ServiceImpl.java (implementation)
├── repository/
│   └── {Name}Repository.java
├── domain/
│   ├── entity/{Name}.java
│   ├── dto/{Name}RequestDto.java
│   ├── dto/{Name}ResponseDto.java
│   └── mapper/{Name}Mapper.java    (MapStruct)
├── exception/
│   ├── {Name}NotFoundException.java
│   └── GlobalExceptionHandler.java
├── messaging/
│   ├── producer/{Name}EventPublisher.java
│   └── consumer/{Name}EventConsumer.java
└── security/
    └── SecurityConfig.java
```

---

## 3. Exception Handling

### Custom Exceptions
```java
// Base exception — all custom exceptions extend this
public abstract class ApplicationException extends RuntimeException {
    private final String errorCode;
    public ApplicationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

// Resource not found
public class ResourceNotFoundException extends ApplicationException {
    public ResourceNotFoundException(String resourceName, UUID id) {
        super(String.format("%s not found: %s", resourceName, id), "RESOURCE_NOT_FOUND");
    }
}
```

### Global Exception Handler (RFC 7807)
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation Failed");
        pd.setDetail(ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", ")));
        pd.setProperty("traceId", MDC.get("traceId"));
        return pd;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Resource Not Found");
        pd.setDetail(ex.getMessage());
        pd.setProperty("traceId", MDC.get("traceId"));
        return pd;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal Server Error");
        pd.setDetail("An unexpected error occurred. Please contact support.");
        pd.setProperty("traceId", MDC.get("traceId"));
        return pd;
    }
}
```

---

## 4. Logging Standards

```java
@Slf4j  // Always use SLF4J via Lombok

// ✓ CORRECT — structured, with context
log.info("Payment created: id={}, reference={}, amount={}", payment.getId(), payment.getReference(), payment.getAmount());
log.warn("Idempotency key already used: key={}", idempotencyKey);
log.error("Failed to publish MQ event: paymentId={}", paymentId, exception);

// ✗ AVOID — no context, string concatenation
log.info("Payment created: " + payment.getId());

// MDC — set at filter level (always present on every log line)
MDC.put("traceId", UUID.randomUUID().toString());
MDC.put("userId", SecurityContextHolder.getContext().getAuthentication().getName());
```

### Logback Configuration (logback-spring.xml)
```xml
<encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <includeMdcKeyName>traceId</includeMdcKeyName>
    <includeMdcKeyName>userId</includeMdcKeyName>
    <includeMdcKeyName>requestId</includeMdcKeyName>
</encoder>
```

---

## 5. Security Standards

### Mandatory Controls
| Control | Implementation |
|---------|---------------|
| Authentication | Spring Security OAuth2 Resource Server |
| Authorization | `@PreAuthorize("hasAuthority('SCOPE_write')")` |
| Input validation | `@Valid` on all controller parameters |
| SQL injection | Spring Data JPA only — no native string-concat queries |
| XSS | Jackson default — no raw HTML in responses |
| Secrets | Never in code or config files — Vault / AWS SM only |
| CORS | Configured explicitly — no wildcard `*` in production |
| TLS | Enforced at gateway — minimum TLS 1.2 |

### Forbidden Patterns
```java
// ✗ NEVER — hardcoded credentials
String password = "SuperSecret123";

// ✗ NEVER — native SQL with string concatenation
entityManager.createNativeQuery("SELECT * FROM users WHERE id = " + userId);

// ✗ NEVER — logging sensitive data
log.info("Payment card number: {}", cardNumber);

// ✗ NEVER — catch and swallow exceptions
try { ... } catch (Exception e) { /* do nothing */ }
```

---

## 6. Database Standards

- **Migrations:** Flyway — scripts versioned and never modified after release
- **Naming:** snake_case for tables and columns
- **Primary Keys:** UUID (`gen_random_uuid()`) — never sequential integers
- **Timestamps:** `TIMESTAMP WITH TIME ZONE` — always store in UTC
- **Soft Delete:** Use `deleted_at TIMESTAMP` — never physical delete of business data
- **Optimistic Locking:** `@Version` annotation on all entities
- **No ORM for Reports:** Use `@Query` with projections or native SQL for read-heavy queries

---

## 7. Testing Standards

| Type | Framework | Coverage Target |
|------|----------|----------------|
| Unit | JUnit 5 + Mockito | ≥ 85% line/branch |
| Integration | Spring Boot Test + Testcontainers | All AC scenarios |
| Contract | Pact | All consumed APIs |
| E2E | REST-Assured | Happy path + critical failures |

### Unit Test Structure (AAA Pattern)
```java
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository repository;
    @Mock PaymentMapper mapper;
    @InjectMocks PaymentServiceImpl service;

    @Test
    @DisplayName("create: should persist and return payment when valid request provided")
    void create_validRequest_shouldPersistAndReturn() {
        // ARRANGE
        var request = buildValidRequest();
        var entity = buildEntity();
        var response = buildResponse();
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(response);

        // ACT
        var result = service.create(request);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
        verify(repository).save(entity);
    }
}
```

---

## 8. Git & PR Standards

- **Branch naming:** `feature/LRS-{id}-{short-desc}` | `bugfix/LRS-{id}-{desc}` | `hotfix/{desc}`
- **Commit messages:** `[LRS-{id}] {type}: {description}` — e.g. `[LRS-001] feat: add payment creation endpoint`
- **PR size:** Max 400 lines changed per PR (split larger work)
- **PR checklist:** Must include testing evidence (coverage screenshot or report link)
- **Squash merge:** Always — keep main branch history clean
