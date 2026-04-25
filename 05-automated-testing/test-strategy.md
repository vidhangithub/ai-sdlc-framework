# Test Strategy — AI-SDLC Framework

> **LRS Reference:** All requirements
> **Version:** 1.0.0
> **Owner:** QA Lead / Engineering Lead

---

## 1. Testing Philosophy

- **Shift-left:** Tests written alongside (or before) code — never after
- **AI-generated, human-reviewed:** All test stubs generated from LLD and ACs; engineers validate and extend
- **Full traceability:** Every test tagged with LRS requirement ID
- **Zero tolerance:** Pipeline blocks on any test failure or quality gate breach

---

## 2. Test Pyramid

```
         ┌─────────────────┐
         │   E2E Tests (5%) │  ← REST-Assured / Playwright
         │   Slow, brittle  │
         ├─────────────────┤
         │  Integration    │  ← Testcontainers + WireMock
         │  Tests (20%)    │
         ├─────────────────┤
         │                 │
         │  Unit Tests     │  ← JUnit 5 + Mockito
         │     (75%)       │
         │  Fast, isolated │
         └─────────────────┘
```

---

## 3. Unit Testing

### Framework & Tools
| Tool | Version | Purpose |
|------|---------|---------|
| JUnit 5 | 5.10+ | Test runner |
| Mockito | 5.x | Mocking |
| AssertJ | 3.x | Fluent assertions |
| JaCoCo | 0.8.11 | Coverage reporting |

### Coverage Targets
| Scope | Target |
|-------|--------|
| Line coverage | ≥ 85% |
| Branch coverage | ≥ 80% |
| Method coverage | ≥ 90% |
| Service layer | ≥ 95% |

### What to Unit Test
- All service methods (happy path + all failure paths)
- All mapper methods (edge cases: null, empty collections)
- Utility classes
- Domain object validation logic

### What NOT to Unit Test
- Spring Boot wiring (covered by integration tests)
- JPA repositories directly (use @DataJpaTest or integration tests)
- Simple getters/setters (Lombok-generated)

### Example Unit Test Structure
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService — Unit Tests")
class PaymentServiceTest {

    @Mock private PaymentRepository repository;
    @Mock private PaymentMapper mapper;
    @Mock private PaymentEventPublisher eventPublisher;
    @InjectMocks private PaymentServiceImpl service;

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create payment and publish event when valid request")
        void shouldCreatePaymentAndPublishEvent() {
            // Arrange
            var request = PaymentRequestDto.builder().reference("REF-001").amount(BigDecimal.TEN).currency("GBP").build();
            var entity = new Payment();
            var savedEntity = new Payment();
            var response = PaymentResponseDto.builder().id(UUID.randomUUID()).build();

            when(mapper.toEntity(request)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(savedEntity);
            when(mapper.toDto(savedEntity)).thenReturn(response);

            // Act
            var result = service.create(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            verify(repository).save(entity);
            verify(eventPublisher).publishCreated(savedEntity);
        }

        @Test
        @DisplayName("should throw DuplicatePaymentException when idempotency key already used")
        void shouldThrowWhenIdempotencyKeyAlreadyUsed() {
            // Arrange
            var request = PaymentRequestDto.builder().idempotencyKey("key-123").build();
            when(repository.findByIdempotencyKey("key-123")).thenReturn(Optional.of(new Payment()));

            // Act & Assert
            assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(DuplicatePaymentException.class)
                .hasMessageContaining("key-123");

            verify(repository, never()).save(any());
        }
    }
}
```

---

## 4. Integration Testing

### Framework & Tools
| Tool | Purpose |
|------|---------|
| @SpringBootTest | Full application context |
| Testcontainers | Real PostgreSQL, IBM MQ, Redis |
| WireMock | External HTTP service stubs |
| TestRestTemplate | HTTP calls in tests |
| Flyway | DB migration in test context |

### Test Configuration
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
@Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
class PaymentIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Container
    static MQContainer mq = new MQContainer("icr.io/ibm-messaging/mq:9.4.0.0")
        .withEnv("MQ_QMGR_NAME", "QM1")
        .withEnv("MQ_APP_USER", "app")
        .withEnv("MQ_APP_PASSWORD", "testpassword");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("ibm.mq.host", mq::getHost);
        registry.add("ibm.mq.port", () -> mq.getMappedPort(1414));
    }
}
```

---

## 5. Contract Testing (Pact)

### Consumer-Driven Contract Tests
- Each service publishes a Pact contract describing what it consumes from downstream APIs
- Downstream APIs run provider verification tests against published contracts
- Pact Broker hosts all contracts centrally

```java
// Consumer test — defines the contract
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "payment-service")
class PaymentConsumerPactTest {

    @Pact(consumer = "notification-service")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        return builder
            .given("payment exists with id abc-123")
            .uponReceiving("a request to get payment by id")
            .path("/api/v1/payments/abc-123")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(newJsonBody(body -> {
                body.uuid("id");
                body.stringType("reference");
                body.decimalType("amount");
            }).build())
            .toPact();
    }
}
```

---

## 6. Performance Testing (Gatling)

### Test Scenarios
| Scenario | Users | Duration | P95 SLA | Error Rate SLA |
|---------|-------|---------|---------|----------------|
| Baseline | 10 | 2 min | < 300ms | < 0.5% |
| Normal Load | 50 | 5 min | < 500ms | < 1% |
| Peak Load | 100 | 5 min | < 800ms | < 2% |
| Spike | 5→200→5 | 5 min | < 1000ms | < 5% |

### Running Performance Tests
```bash
# Run Gatling simulation
mvn gatling:test -Dgatling.simulationClass=PaymentSimulation -Denv=perf

# View HTML report
open target/gatling/paymentsimulation-*/index.html
```

---

## 7. Security Testing (DAST)

### OWASP ZAP Automated Scan
```bash
# Full active scan against deployed service
docker run --network host owasp/zap2docker-stable zap-full-scan.py \
  -t https://payment-service.sit.internal \
  -r zap-report.html \
  -x zap-report.xml \
  --hook=/zap/auth-hook.py

# Pipeline: fail on HIGH or CRITICAL findings
zap-cli check-alerts --alert-level High
```

### Container Image Scanning (Trivy)
```bash
trivy image \
  --exit-code 1 \
  --severity HIGH,CRITICAL \
  --format table \
  registry.company.com/payment-service:${BUILD_NUMBER}
```

---

## 8. Test Data Management

| Rule | Detail |
|------|--------|
| No real PII | All test data synthetic or anonymised |
| Seeded per test | Testcontainers + @Sql scripts — isolated per test |
| No shared state | Each integration test cleans up after itself |
| Feeder files | Gatling uses parameterised CSV feeders |
| WireMock | Stubs from sanitised real API recordings |

---

## 9. Test Reporting

| Report | Tool | Location | Retained |
|--------|------|---------|---------|
| Unit test results | Surefire XML + JUnit report | Jenkins / Harness | 90 days |
| Coverage | JaCoCo HTML | SonarQube | Permanent |
| Integration results | Surefire XML | Jenkins | 90 days |
| Performance report | Gatling HTML | Artifactory | Per release |
| ZAP DAST report | HTML + XML | Artifactory | Per release |
| Trivy image scan | JSON | Pipeline artifact | Per release |
