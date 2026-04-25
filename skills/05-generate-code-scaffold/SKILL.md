---
name: generate-code-scaffold
version: 1.0.0
phase: "04 — Code Development"
description: >
  Generates a complete, company-standards-compliant Spring Boot 3.x service
  scaffold from an approved LLD. Use this skill whenever a user wants to
  generate Java code, create a Spring Boot service, scaffold a REST API,
  create boilerplate code, or bootstrap a new microservice. Trigger when the
  user says "generate code", "create the service", "scaffold this", "write
  the Spring Boot code", "implement this endpoint", "create the Java classes",
  "bootstrap the service", or "generate the boilerplate". Always generate from
  the LLD — never from raw requirements. Output is a starting point for
  engineers, not production-ready code — human review is mandatory.
inputs:
  - name: lld
    description: Approved Low-Level Design document
    required: true
  - name: service_name
    description: "Service name in kebab-case, e.g. payment-service"
    required: true
  - name: package_base
    description: "Java package base, e.g. com.company.payments"
    required: true
  - name: entity_name
    description: "Primary domain entity name in PascalCase, e.g. Payment"
    required: true
  - name: features
    description: "Comma-separated: rest_api, jpa, ibm_mq, redis, security, flyway (default: all)"
    required: false
    default: "rest_api,jpa,ibm_mq,security,flyway"
output: Complete Java source files for a Spring Boot 3.x service
---

# Skill: Generate Spring Boot Code Scaffold

## Purpose

Generate a complete, engineer-reviewable Spring Boot 3.x service scaffold that follows company coding standards exactly. The scaffold covers every layer from controller to database migration, with unit test stubs included.

---

## Step-by-Step Instructions

### Step 1 — Read the LLD

Extract from the LLD:
- All API endpoints (method, path, request/response DTOs, auth scope)
- Entity fields and types
- Database table definition
- Any MQ topics (inbound/outbound)
- Configuration properties needed
- Security requirements (OAuth2 scopes)

### Step 2 — Generate pom.xml Dependencies

Include exactly the dependencies needed based on `features` input:

```xml
<!-- Always included -->
<dependency>spring-boot-starter-web</dependency>
<dependency>spring-boot-starter-validation</dependency>
<dependency>spring-boot-starter-actuator</dependency>
<dependency>spring-boot-starter-security</dependency>
<dependency>spring-security-oauth2-resource-server</dependency>
<dependency>spring-security-oauth2-jose</dependency>
<dependency>lombok</dependency>
<dependency>mapstruct</dependency>
<dependency>logstash-logback-encoder</dependency>

<!-- if jpa feature -->
<dependency>spring-boot-starter-data-jpa</dependency>
<dependency>postgresql</dependency>

<!-- if flyway feature -->
<dependency>flyway-core</dependency>
<dependency>flyway-database-postgresql</dependency>

<!-- if ibm_mq feature -->
<dependency>spring-boot-starter-artemis</dependency>
<!-- OR: mq-jms-spring-boot-starter for IBM MQ -->

<!-- if redis feature -->
<dependency>spring-boot-starter-data-redis</dependency>

<!-- Test scope - always included -->
<dependency>spring-boot-starter-test</dependency>
<dependency>mockito-core</dependency>
<dependency>testcontainers</dependency>
<dependency>wiremock-standalone</dependency>
```

### Step 3 — Generate Each Class

Produce every class following the patterns below. Output each file with a clear header comment showing file path.

---

#### A. Entity Class

```java
// File: src/main/java/{package}/domain/entity/{EntityName}.java
package {package_base}.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "{table_name}", indexes = {
    @Index(name = "idx_{table}_status", columnList = "status"),
    @Index(name = "idx_{table}_created_at", columnList = "created_at")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class {EntityName} {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // --- Add LLD-derived fields here ---
    // Example:
    // @Column(name = "reference", nullable = false, length = 100)
    // private String reference;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private {EntityName}Status status;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
```

#### B. Request DTO

```java
// File: src/main/java/{package}/domain/dto/{EntityName}RequestDto.java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class {EntityName}RequestDto {

    @NotBlank(message = "Reference must not be blank")
    @Size(max = 100, message = "Reference must not exceed 100 characters")
    private String reference;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Digits(integer = 15, fraction = 2)
    private BigDecimal amount;

    // Add all fields from LLD DTO definition
}
```

#### C. Response DTO

```java
// File: src/main/java/{package}/domain/dto/{EntityName}ResponseDto.java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class {EntityName}ResponseDto {

    private UUID id;
    private String reference;
    // Add all LLD response fields

    private OffsetDateTime createdAt;
    private String status;
}
```

#### D. MapStruct Mapper

```java
// File: src/main/java/{package}/domain/mapper/{EntityName}Mapper.java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface {EntityName}Mapper {

    {EntityName} toEntity({EntityName}RequestDto dto);
    {EntityName}ResponseDto toDto({EntityName} entity);
    List<{EntityName}ResponseDto> toDtoList(List<{EntityName}> entities);
}
```

#### E. Repository

```java
// File: src/main/java/{package}/repository/{EntityName}Repository.java
@Repository
public interface {EntityName}Repository extends JpaRepository<{EntityName}, UUID> {

    Optional<{EntityName}> findByIdAndDeletedAtIsNull(UUID id);
    Optional<{EntityName}> findByIdempotencyKey(String idempotencyKey);
    Page<{EntityName}> findByStatusAndDeletedAtIsNull(
        {EntityName}Status status, Pageable pageable);
}
```

#### F. Service Interface + Implementation

```java
// File: src/main/java/{package}/service/{EntityName}Service.java
public interface {EntityName}Service {
    {EntityName}ResponseDto create({EntityName}RequestDto request);
    {EntityName}ResponseDto getById(UUID id);
    // Add methods per LLD
}

// File: src/main/java/{package}/service/impl/{EntityName}ServiceImpl.java
@Service @RequiredArgsConstructor @Slf4j
@Transactional(readOnly = true)
public class {EntityName}ServiceImpl implements {EntityName}Service {

    private final {EntityName}Repository repository;
    private final {EntityName}Mapper mapper;
    // Inject event publisher if MQ enabled

    @Override
    @Transactional
    public {EntityName}ResponseDto create({EntityName}RequestDto request) {
        log.info("Creating {entityName}: reference={}", request.getReference());

        // 1. Idempotency check (if applicable)
        // 2. Map DTO → Entity
        var entity = mapper.toEntity(request);
        entity.setStatus({EntityName}Status.PENDING);
        entity.setCreatedBy(getCurrentUserId());

        // 3. Persist
        var saved = repository.save(entity);
        log.info("{EntityName} created: id={}", saved.getId());

        // 4. Publish event (if MQ enabled)
        // eventPublisher.publishCreated(saved);

        // 5. Return DTO
        return mapper.toDto(saved);
    }

    @Override
    public {EntityName}ResponseDto getById(UUID id) {
        var entity = repository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new {EntityName}NotFoundException(id));
        return mapper.toDto(entity);
    }

    private String getCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .map(Authentication::getName)
            .orElse("system");
    }
}
```

#### G. Controller

```java
// File: src/main/java/{package}/controller/{EntityName}Controller.java
@RestController
@RequestMapping("/api/v1/{resource-path}")
@RequiredArgsConstructor @Slf4j @Validated
@Tag(name = "{EntityName}", description = "{EntityName} management API")
public class {EntityName}Controller {

    private final {EntityName}Service service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create {entityName}")
    @PreAuthorize("hasAuthority('SCOPE_write')")
    public {EntityName}ResponseDto create(
            @Valid @RequestBody {EntityName}RequestDto request) {
        log.info("POST /api/v1/{resource-path}");
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get {entityName} by ID")
    @PreAuthorize("hasAuthority('SCOPE_read')")
    public {EntityName}ResponseDto getById(@PathVariable UUID id) {
        log.info("GET /api/v1/{resource-path}/{}", id);
        return service.getById(id);
    }
}
```

#### H. Exception Classes

```java
// File: src/main/java/{package}/exception/{EntityName}NotFoundException.java
public class {EntityName}NotFoundException extends RuntimeException {
    public {EntityName}NotFoundException(UUID id) {
        super(String.format("{EntityName} not found: %s", id));
    }
}

// File: src/main/java/{package}/exception/GlobalExceptionHandler.java
@RestControllerAdvice @Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex,
                                          HttpServletRequest request) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation Failed");
        pd.setDetail(ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", ")));
        pd.setProperty("traceId", MDC.get("traceId"));
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    @ExceptionHandler({EntityName}NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNotFound({EntityName}NotFoundException ex,
                                         HttpServletRequest request) {
        var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Resource Not Found");
        pd.setDetail(ex.getMessage());
        pd.setProperty("traceId", MDC.get("traceId"));
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: traceId={}", MDC.get("traceId"), ex);
        var pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal Server Error");
        pd.setDetail("An unexpected error occurred. Reference: " + MDC.get("traceId"));
        pd.setProperty("traceId", MDC.get("traceId"));
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }
}
```

#### I. Security Config

```java
// File: src/main/java/{package}/config/SecurityConfig.java
@Configuration @EnableWebSecurity @EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .headers(h -> h
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true))
            )
            .build();
    }
}
```

#### J. Flyway Migration

```sql
-- File: src/main/resources/db/migration/V1__{service_name}_initial_schema.sql
-- LRS Reference: {LRS-ID}
-- Description: Initial schema for {service_name}

CREATE TABLE {table_name} (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Add LLD-derived columns here
    reference   VARCHAR(100) NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    deleted_at  TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(100) NOT NULL,
    version     BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_{table_name}_status     ON {table_name}(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_{table_name}_created_at ON {table_name}(created_at);
```

#### K. Unit Test Stub

```java
// File: src/test/java/{package}/service/{EntityName}ServiceTest.java
@ExtendWith(MockitoExtension.class)
@DisplayName("{EntityName}Service — Unit Tests")
class {EntityName}ServiceTest {

    @Mock {EntityName}Repository repository;
    @Mock {EntityName}Mapper mapper;
    @InjectMocks {EntityName}ServiceImpl service;

    @Nested @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should persist and return DTO when valid request provided")
        void create_validRequest_shouldPersistAndReturnDto() {
            // ARRANGE
            var request = {EntityName}RequestDto.builder()
                .reference("REF-001")
                // add fields
                .build();
            var entity = new {EntityName}();
            var saved  = new {EntityName}();
            var dto    = {EntityName}ResponseDto.builder().id(UUID.randomUUID()).build();

            when(mapper.toEntity(request)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(saved);
            when(mapper.toDto(saved)).thenReturn(dto);

            // ACT
            var result = service.create(request);

            // ASSERT
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("should throw {EntityName}NotFoundException when ID not found")
        void getById_notFound_shouldThrowNotFoundException() {
            // ARRANGE
            var id = UUID.randomUUID();
            when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf({EntityName}NotFoundException.class)
                .hasMessageContaining(id.toString());
        }
    }
}
```

### Step 4 — Output Format

Output each file clearly separated with a header:

```
// ════════════════════════════════════════════════════════
// FILE: src/main/java/com/company/.../ClassName.java
// ════════════════════════════════════════════════════════
[full class content]
```

After all files, output a summary table:

| File | Status | Notes |
|------|--------|-------|
| {EntityName}.java | Generated | Add LLD fields at marked comment |
| {EntityName}RequestDto.java | Generated | Validation annotations added |
| ... | | |

Then add a **"TODO for Engineer"** section listing everything the engineer must complete before PR:
- Fill in entity fields from LLD
- Complete business logic in ServiceImpl
- Complete unit test scenarios for all AC scenarios
- Add any custom repository queries
- Verify MapStruct mappings are complete

---

## Quality Checklist

- [ ] All files use `jakarta.*` namespace (not `javax.*`)
- [ ] All classes use Lombok — no manual getters/setters
- [ ] Entity has: UUID PK, `@Version`, audit timestamps, `deletedAt`
- [ ] Controller uses `@Valid` on all request bodies
- [ ] Controller uses `@PreAuthorize` for all endpoints
- [ ] Service implementation uses `@Transactional` on write methods
- [ ] GlobalExceptionHandler covers: validation, not-found, generic
- [ ] All error responses use Spring 6 `ProblemDetail` (RFC 7807)
- [ ] MDC traceId referenced in log statements
- [ ] Unit test stubs follow AAA pattern with meaningful display names
- [ ] Flyway migration uses `gen_random_uuid()` not sequences

---

## Rules

- **Never use `javax.*`** — always `jakarta.*` (Spring Boot 3.x)
- **Never use field injection `@Autowired`** — always constructor injection via `@RequiredArgsConstructor`
- **Never return entities from controllers** — always DTOs
- **Never log sensitive data** (passwords, card numbers, tokens)
- **Never use `String` for UUIDs in APIs** — use `UUID` type
- **Never catch and swallow exceptions** — always log and rethrow or translate
