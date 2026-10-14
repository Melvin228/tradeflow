# TradeFlow Backend Architecture

## 1. Purpose

This document defines the architecture and engineering practices for the TradeFlow backend.

TradeFlow is a simulated portfolio and trade-order processing platform. The first release is a modular monolith focused on correctness, transactional consistency, concurrency safety, testability, and operational visibility.

This is a living document. Decisions may change when measurements or new requirements justify a change, but changes must be recorded rather than introduced implicitly.

## 2. Technology Baseline

- Java 25
- Spring Boot 4.x
- Spring Web MVC
- Spring Data JPA and Hibernate
- PostgreSQL
- Flyway
- Maven
- JUnit 5
- Testcontainers
- Spring Boot Actuator and Micrometer
- Docker Compose for local infrastructure

Spring Boot's dependency management must control Spring and third-party dependency versions wherever possible. Avoid overriding managed dependency versions unless there is a documented compatibility or security reason.

## 3. Architectural Style

TradeFlow starts as a **modular monolith**.

Reasons:

- The business model and boundaries are still evolving.
- Local transactions are valuable for cash, positions, orders, and trades.
- One deployable application is easier to build, test, observe, and finish.
- Microservices would introduce distributed transactions, network failures, deployment overhead, and eventual consistency before they are justified.

Modules should communicate through public application-layer interfaces or domain events, not by directly accessing another module's internal classes.

## 4. Package Organization

Use package-by-feature, with layers inside each feature:

```text
com.melvin.tradeflow
├── portfolio
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── instrument
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── order
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── position
├── trade
├── processing
├── common
└── configuration
```

Responsibilities:

- `api`: controllers, HTTP request/response records, API mapping.
- `application`: use-case orchestration and transaction boundaries.
- `domain`: business rules, domain types, and domain-specific exceptions.
- `infrastructure`: JPA repositories, persistence adapters, messaging adapters, and external integrations.

Avoid generic top-level packages such as one global `controller`, `service`, and `repository` package. Feature-based organization keeps related code together and makes future module extraction easier.

Within a feature, keep related HTTP boundary types together in that feature's `api` package while it remains navigable. For example:

```text
user/
└── api/
    ├── UserController.java
    ├── RegisterUserRequest.java
    ├── UserResponse.java
    ├── UserApiRoutes.java
    └── UserApiExceptionHandler.java
```

It is acceptable for a controller, its request/response records, route constants, and feature-specific exception mapping to share this package. Do not create `request`, `response`, and `controller` subpackages solely to give each type category its own folder. If a feature's API grows enough that the package becomes difficult to navigate, split it along actual endpoint or subfeature boundaries while keeping ownership clear.

Put cross-feature HTTP concerns, such as a global exception handler or shared API error model, in `common.api`. Keep feature-specific errors and their HTTP translation with the owning feature unless the mapping is intentionally uniform across the application. Do not put all feature controllers or DTOs into a single global `api` package.

## 5. Request Flow

```text
HTTP request
→ Controller
→ Application service
→ Domain rules
→ Repository
→ PostgreSQL
→ Response DTO
```

Rules:

- Controllers handle HTTP concerns only.
- Controllers must not contain business logic or manage transactions.
- Application services define use cases and transaction boundaries.
- Domain rules should be explicit and independently testable.
- Repositories handle persistence, not business decisions.
- JPA entities must not be returned directly from controllers.
- Map entities to dedicated API response records.

## 6. Spring MVC Versus WebFlux

TradeFlow will use **Spring Web MVC**.

The initial persistence stack uses JPA and JDBC, which are blocking APIs. Running blocking persistence calls on WebFlux event-loop threads would undermine the reactive concurrency model and could reduce reliability. WebFlux should be considered only if the complete request path can be non-blocking and there is a measured need for high concurrency with many waiting network operations.

Choosing WebFlux does not automatically make an application faster. It changes the programming and concurrency model and increases debugging and testing complexity.

The project may still use `WebClient` for a future remote integration while retaining MVC controllers.

## 7. Java 25 Practices

### 7.1 Records

Use records for immutable data carriers:

- HTTP request and response DTOs
- Application commands and query results
- Domain events
- Small value objects when their invariants can be enforced in the compact constructor

Example:

```java
public record CreatePortfolioRequest(
        @NotBlank String name,
        @NotNull @PositiveOrZero BigDecimal initialCash
) {
}
```

Do not use records for JPA entities. JPA entities require an identity and lifecycle, are generally mutable, and must work with persistence-provider construction and proxy behavior.

Records improve clarity and immutability; they are not automatically a performance optimization.

### 7.2 Pattern Matching and Switch Expressions

Use pattern matching and switch expressions when they make domain handling clearer. Do not introduce sealed hierarchies or complex switch logic merely to demonstrate a language feature.

### 7.3 Virtual Threads

Virtual threads may be evaluated later for high-concurrency blocking I/O workloads. They are not required for the MVP.

Guidelines:

- Measure before and after enabling them.
- Do not expect improvements for CPU-bound calculations.
- Database connections, broker channels, and downstream capacity remain limited resources.
- Continue bounding access to scarce resources even when threads are inexpensive.
- Avoid long blocking operations while holding a `synchronized` monitor.
- Do not mix several concurrency models without a specific reason.

Virtual threads do not remove the need to understand race conditions, transactions, locks, atomicity, idempotency, or backpressure.

### 7.4 Constants and Repeated Values

Use a named constant when a value is reused and has one stable meaning, or when naming a domain rule makes its purpose clearer. Prefer method-local variables for intermediate values, a `private static final` field for constants reused within one class, and a feature-scoped type only when multiple classes genuinely share the same rule.

Guidelines:

- Use descriptive `UPPER_SNAKE_CASE` names for Java constants.
- Do not create a catch-all `Constants` class or promote every literal into a constant. Equal values are not necessarily the same concept; share a constant only when the meaning and change policy are shared.
- Name non-obvious domain limits, units, or algorithm identifiers. Keep obvious one-off values inline when a name would add indirection without clarifying intent.
- Use enums for a closed set of domain choices and configuration properties for environment-specific or operationally tunable values. Do not hard-code secrets or deployment settings as constants.
- Keep constants immutable and scoped to their owning feature. Avoid public constants as an alternative to a clear module API.
- Values used in annotation attributes must be compile-time constants. If an API validation limit and a domain/persistence rule are intentionally the same rule, place the shared compile-time constant in an appropriately scoped feature type.
- Treat SQL migrations as the schema source of truth. Java constants cannot be shared with migration scripts, so keep corresponding schema limits and named constraints aligned deliberately and cover important drift with database integration tests. Never rewrite an already-applied migration just to change a constant.
- In tests, name repeated fixtures when it improves readability, but keep scenario-specific values close to the assertion that explains them.

Examples from the current codebase:

- `UserApiRoutes.BASE` is appropriately scoped and reused by the request mapping and security rule. `AuthApiRoutes` keeps authentication route paths together for the endpoints that will use them.
- The email maximum (`320`) and display-name maximum (`120`) are repeated in request validation and `UserEntity` persistence/validation. These are candidates for feature-scoped shared compile-time constants if they are intended to remain one rule. The password-hash storage maximum (`255`) is also repeated in persistence mapping and validation, while the `128` password-input maximum constrains a different value and must stay separate.
- `uk_app_user_email` is named in both the registration service and the user-table migration. Keep the Java constant for application-side recognition, and retain the matching database name in the migration; there is no shared constant across Java and SQL.
- The `pbkdf2` encoder identifier appears in the password-encoder configuration more than once. A private constant there can keep the map key and encoder id together if that identifier changes.
- `NUMERIC(19,4)` and `NUMERIC(19,8)` recur across migrations as monetary and quantity schema policies. Keep their use intentional and consistent with the documented domain precision; SQL DDL has no shared Java constant to reference.

## 8. Spring Boot Practices

### 8.1 Dependency Injection

- Prefer constructor injection.
- Declare mandatory dependencies as `final`.
- Avoid field injection.
- Avoid retrieving beans manually from `ApplicationContext` in business code.
- Use `@Qualifier` only when several implementations genuinely coexist.

### 8.2 Configuration

- Store environment-specific values outside the compiled application.
- Use `@ConfigurationProperties` for grouped, type-safe configuration.
- Validate required configuration at startup.
- Never commit credentials or production secrets.
- Use environment variables or an external secret store for secrets.

### 8.3 Transactions

- Place transaction boundaries on public application-service methods.
- Keep transactions as short as reasonably possible.
- Do not perform slow network calls inside database transactions.
- Understand that proxy-based `@Transactional` does not apply through normal self-invocation.
- Use `@Transactional(readOnly = true)` for query use cases where appropriate.
- Specify propagation or isolation only when the business requirement demands it.
- Do not catch an exception inside a transactional method and silently continue if the transaction should roll back.

### 8.4 Entities and Lombok

- Avoid Lombok `@Data` on JPA entities.
- Entity `equals`, `hashCode`, and `toString` require deliberate implementation.
- Do not include lazy associations in `toString`, `equals`, or `hashCode`.
- Prefer explicit entity behavior over exposing setters for every field.
- Use DTO records instead of using entities as API models.

### 8.5 Exceptions and API Errors

- Use domain-specific exceptions for meaningful business failures.
- Translate exceptions at the API boundary with `@RestControllerAdvice`.
- Return consistent error responses using `ProblemDetail` or an equivalent documented schema.
- Do not leak stack traces, SQL, or internal implementation details to clients.

## 9. Money and Time

- Never use `double` or `float` for money.
- Use `BigDecimal` with an explicit scale and rounding policy.
- Use `compareTo` rather than `equals` for numeric comparison when scale differences should not matter.
- Persist monetary amounts using an explicit `NUMERIC` precision and scale.
- Store timestamps as UTC `Instant` values.
- Convert time zones only at system boundaries.

Recommended initial PostgreSQL type for simulated monetary amounts:

```sql
NUMERIC(19, 4)
```

The scale and supported currencies must be revisited before supporting real financial instruments or multi-currency portfolios.

## 10. Database Schema and Indexing

### 10.1 Constraints First

Use database constraints to enforce invariants that must remain true under concurrency:

- Primary keys
- Foreign keys
- Unique instrument symbol
- Unique idempotency key
- Unique position per portfolio and instrument
- Check constraints where they express stable database-level invariants

Example:

```sql
CONSTRAINT uk_order_idempotency UNIQUE (idempotency_key)
```

### 10.2 Index Strategy

Indexes should support actual query patterns, not be added to every column.

Initial candidates:

```text
instrument(symbol) UNIQUE
trade_order(idempotency_key) UNIQUE
position(portfolio_id, instrument_id) UNIQUE
trade_order(portfolio_id, created_at)
trade(order_id)
```

Foreign-key columns used in joins should be evaluated for indexes because PostgreSQL does not automatically create an index for every foreign key.

Guidelines:

- Prefer composite indexes that match filtering and ordering patterns.
- Column order in a composite index matters.
- Every index increases storage and write cost.
- Verify important queries with `EXPLAIN (ANALYZE, BUFFERS)` using representative data.
- Do not claim an index improves performance without measuring the execution plan.

## 11. Flyway Migration Strategy

Flyway owns the database schema. Hibernate must validate the schema rather than generate or modify it.

Recommended configuration:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Runtime migration location:

```text
backend/src/main/resources/db/migration/
├── V1__create_portfolio_and_instrument.sql
├── V2__create_order_position_and_trade.sql
├── V3__add_order_idempotency_key.sql
└── R__reference_instruments.sql
```

Draft scripts that are not ready to execute may live outside the configured Flyway path:

```text
docs/database/drafts/
```

Do **not** maintain separate `master`, `executed`, and `pending` folders by moving migration files after execution. Flyway's `flyway_schema_history` table is the authoritative record of applied and pending migrations. All released versioned migrations must remain available in source control and in the configured migration location.

Rules:

- Versioned migrations use `V<version>__<description>.sql`.
- Every version is unique and ordered.
- Once a migration has been applied to a permanent shared environment, never modify it.
- Correct mistakes by creating a new forward migration.
- Use repeatable migrations (`R__...sql`) only for safely repeatable objects such as views, functions, or reference-data replacement.
- Repeatable migrations must be idempotent.
- Review migration SQL in the same pull request as the code that depends on it.
- Avoid destructive changes without a compatibility and rollback plan.

## 12. Concurrency and Consistency

Correctness must not depend on a single application instance or an in-memory lock.

### 12.1 Database as the Source of Truth

Use database transactions, constraints, and locking/versioning to protect financial state.

Initial strategy:

- Add `@Version` to portfolio and position entities.
- Detect optimistic-lock conflicts.
- Retry only when the operation is safe and retry count is bounded.
- Use a unique constraint for idempotency.
- Reject operations that would produce a negative balance or position.

Do not use a static `synchronized` block or local `ConcurrentHashMap` as the primary protection for account balances. Those mechanisms do not coordinate multiple application instances.

### 12.2 Thread Pools

The synchronous MVP will not introduce a custom executor.

When asynchronous processing is added:

- Use an explicitly named executor.
- Configure a bounded queue.
- Define core size, maximum size, keep-alive time, and rejection policy deliberately.
- Name threads for diagnostics.
- Expose pool and queue metrics.
- Implement graceful shutdown.
- Do not use the common fork-join pool implicitly for blocking work.
- Do not submit unbounded work to an unbounded queue.

### 12.3 Ordering

Define ordering requirements precisely:

- Global ordering is expensive and reduces parallelism.
- Prefer ordering per portfolio, account, or order aggregate.
- When messaging is introduced, use the aggregate identifier as a partition or grouping key.
- Consumers must tolerate redelivery and retry-related reordering where the broker does not guarantee otherwise.
- Add sequence numbers only when the domain requires detection of stale or missing events.

### 12.4 Idempotency

Every externally submitted order will eventually require an idempotency key.

- Enforce uniqueness in PostgreSQL.
- Return the existing result for an already accepted key when request semantics permit it.
- Consumers must also be idempotent because at-least-once delivery can produce duplicates.
- A later inbox/outbox design may record processed messages and unpublished events.

## 13. Messaging Roadmap

Messaging is not part of the first synchronous release.

When introduced, the design must cover:

- Durable messages and queues/topics
- Producer confirmation
- Consumer acknowledgement after successful durable processing
- Bounded retry with backoff
- Dead-letter handling
- Idempotent consumption
- Per-aggregate ordering requirements
- Correlation and trace identifiers
- Transactional outbox for database-update and message-publication consistency

Avoid claiming end-to-end exactly-once processing. Prefer at-least-once delivery with idempotent business processing.

## 14. API Practices

- Version public APIs, initially under `/api/v1`.
- Use nouns for resources.
- Validate request records with Jakarta Validation.
- Return appropriate HTTP status codes.
- Use pagination for collection endpoints expected to grow.
- Do not expose database identifiers or internal fields unnecessarily.
- Make idempotency behavior explicit in API documentation.
- Generate OpenAPI documentation, but keep business behavior documented in prose and tests.

Example endpoints:

```text
POST /api/v1/portfolios
GET  /api/v1/portfolios/{portfolioId}
GET  /api/v1/instruments
POST /api/v1/orders
GET  /api/v1/orders/{orderId}
```

## 15. Testing Strategy

Testing is part of implementation, not a final phase. Add or update tests alongside each behavior so failures are caught close to the change. Prefer a small number of fast, focused tests at lower levels and a smaller number of high-value tests that exercise Spring and PostgreSQL together.

### 15.1 Test Levels

#### Unit tests

A unit test exercises one class or small piece of business behavior without starting Spring or connecting to a database. These should be the fastest and most numerous tests.

Use them for:

- Domain calculations and state transitions
- Order validation and cash/position rules
- Input normalization and mapping
- Application-service decisions when collaborators can be replaced by simple test doubles

Construct the class directly and provide controlled collaborators. Use JUnit assertions; use Mockito only when a collaborator's behavior needs to be controlled or verified. Do not mock simple value objects or the class under test, and do not mock repositories to claim that a database query or constraint works.

#### Slice tests

A slice test starts only a focused part of Spring, which gives useful framework integration without loading the entire application:

- `@WebMvcTest` for controller routing, JSON serialization, request validation, and HTTP error responses. Mock the application service at this boundary.
- `@DataJpaTest` for entity mappings and repository behavior. Configure it to use PostgreSQL through Testcontainers when verifying PostgreSQL-specific behavior; an embedded database is not an equivalent substitute.

Use slices when they provide a clear benefit over a direct unit test or a full integration test. They still start Spring and are slower than unit tests.

#### Integration tests

An integration test verifies that important application components work together, commonly including Spring configuration, application services, repositories, transactions, and the real database engine. Use PostgreSQL through Testcontainers for:

- Repository queries and entity mappings
- Flyway migrations and Hibernate schema validation
- Transaction commit and rollback behavior
- Unique, foreign-key, check, and other database constraints
- Optimistic locking and idempotency
- Important use cases that span multiple features

Testcontainers starts a disposable PostgreSQL instance for the test run. Tests should use the same PostgreSQL major version as the project’s supported runtime where practical. Do not rely on H2 as proof that PostgreSQL-specific schema, locking, SQL, or queries work correctly.

#### End-to-end tests

An end-to-end test exercises a complete externally visible journey through the application boundary, usually an HTTP request through routing, security, business logic, and persistence. Keep this set small and focused on critical journeys—for example, registering a user, logging in, and making an authenticated request. A test that starts the Spring application and calls a repository directly is an integration test, not an end-to-end test.

### 15.2 What to Test in TradeFlow

Test observable behavior and business invariants rather than private methods or implementation details. Prioritize cases that could lose data, leak access, or violate financial rules:

- User registration normalizes email, hashes the password, and rejects duplicate email addresses.
- Login rejects invalid credentials without disclosing whether an account exists; protected requests enforce authentication and ownership.
- Portfolio creation records its initial balance and ledger entry consistently.
- Orders reject inactive or currency-mismatched instruments, insufficient cash, and insufficient holdings.
- Successful buys and sells update the order, portfolio, position, trade, and ledger atomically.
- Replaying an idempotency key does not execute the order twice; reusing it for a different request is rejected.
- Constraints, transaction rollback, and concurrent updates preserve non-negative balances and positions.
- API validation and error handling return the documented status and response shape without exposing internal details.

For each behavior, cover the normal case, important boundary cases, and expected failures. Avoid asserting incidental implementation details such as private method calls, exact SQL formatting, or how many internal helper methods ran. A refactor that preserves behavior should not require rewriting the tests.

### 15.3 Test Organization and Naming

Mirror the production feature/package structure under `src/test/java`. Keep tests close to the feature they verify and make the test type apparent in the class name:

```text
src/test/java/com/melvin/tradeflow/
├── user/
│   ├── domain/UserRegistrationTest.java
│   └── infrastructure/persistence/UserRepositoryTest.java
├── order/
│   ├── domain/OrderValidationTest.java
│   └── application/OrderExecutionIntegrationTest.java
└── support/
    └── PostgreSqlTestConfiguration.java
```

Use descriptive names that state the scenario and outcome, for example:

```text
register_normalizesEmail_andStoresPasswordHash
buyOrder_withInsufficientCash_isRejectedWithoutChangingBalances
```

Keep setup readable. Prefer small test builders or fixtures when they remove repeated, meaningful setup; avoid building a large test framework before repetition exists. Each test should arrange its own relevant state and not depend on the execution order or data left by another test.

### 15.4 Reliability and Security

- Make tests deterministic: control time, generated identifiers, and external responses where practical; do not use sleeps to coordinate work.
- Give potentially blocking tests explicit timeouts and always release/shut down threads and resources.
- Do not use production credentials, real customer data, live services, or real financial data in tests.
- Never print passwords, raw tokens, or other secrets in test output.
- Assert both the response and important persisted state for transactional use cases.
- Keep database tests isolated and repeatable; use transactions or cleanup strategies deliberately, noting that tests involving multiple transactions or concurrency may not be safely isolated by a test-managed transaction.
- Do not disable constraints or Flyway in a test to make a failing schema test pass.

### 15.5 Concurrency Tests

Concurrency tests must coordinate task start using tools such as `CountDownLatch`, then assert business invariants rather than depending on timing alone. Use separate transactions/connections where needed so the test exercises real database concurrency. Avoid sleeps and assumptions about which thread wins.

Example scenario:

```text
Initial cash: 1,000
20 simultaneous buy attempts costing 100 each
Expected: at most 10 successful executions
Invariant: cash never becomes negative
```

Test the contract (successful operation count and final database state), not a particular thread schedule. Tests must have timeouts so failures cannot hang the build indefinitely.

### 15.6 Development Feedback Loop

For each backlog behavior:

1. Identify the behavior and its important success, boundary, and failure cases.
2. Add focused unit tests for business rules and calculations.
3. Add slice or integration tests where Spring, persistence, security, or PostgreSQL behavior matters.
4. Implement the behavior and run the smallest relevant test selection.
5. Before sharing the change, run the complete test suite with the Maven Wrapper and review failures rather than weakening or deleting valid tests.

From `backend/`, run:

```text
./mvnw test
```

On Windows, use:

```text
mvnw.cmd test
```

During development, Maven can run a single test class with `-Dtest=UserRegistrationTest test`. A passing unit-test run does not replace the PostgreSQL integration tests; ensure the CI/build environment runs the complete suite, including Testcontainers tests.

### 15.7 Test-Driven Development

Test-driven development (TDD) is an optional way to work, not a requirement for every change. In the red-green-refactor cycle:

1. **Red:** write a failing test that describes one desired behavior.
2. **Green:** make the smallest implementation change that passes it.
3. **Refactor:** improve names and structure while keeping the test suite passing.

TDD can be especially helpful for domain rules, validation, and tricky edge cases. For framework wiring or exploratory work, it is also reasonable to implement a small vertical slice and add tests immediately. In either workflow, tests should describe intended behavior and remain in the suite.

## 16. Observability and Operations

- Add Spring Boot Actuator.
- Expose health, readiness, and metrics only as required.
- Secure operational endpoints before external deployment.
- Use structured logs with order, portfolio, and correlation identifiers.
- Never log credentials or sensitive financial data.
- Measure request duration, order-processing duration, rejected orders, failures, optimistic-lock conflicts, and executor saturation.
- Preserve exception causes in internal logs while returning safe API errors.

## 17. Performance Principles

- Correctness comes before throughput.
- Measure before optimizing.
- Use pagination and query projections where appropriate.
- Detect N+1 query problems through tests, SQL observation, and profiling.
- Keep transactions short.
- Size database and HTTP connection pools based on measured demand.
- Add caching only when there is a demonstrated read bottleneck and a clear invalidation strategy.
- Do not use virtual threads, reactive programming, parallel streams, or asynchronous methods merely because they are available.

## 18. Security Baseline

The MVP includes user registration and authentication using Spring Security. Store only adaptive password hashes, issue short-lived access tokens, and store only hashes of opaque refresh tokens. Protect portfolio and order operations with authenticated-user ownership checks. The codebase must also:

- Validate all external input.
- Use parameterized persistence APIs.
- Avoid exposing stack traces.
- Keep dependencies updated.
- Keep secrets outside source control.
- Run containers and deployed processes with minimal privileges.
- Never log passwords, raw tokens, password hashes, or refresh-token hashes.

## 19. Build and Code Quality

- Build and test through Maven Wrapper.
- Keep the main branch buildable.
- Use small, reviewable commits.
- Prefer clear code over clever abstraction.
- Avoid premature generic frameworks inside the project.
- Treat warnings, failing tests, migration validation failures, and static-analysis failures as build problems.
- Add formatting and static analysis once the initial application builds reliably.
- Document architectural decisions in `docs/decisions/` when they materially affect the system.

## 20. Initial Architectural Decisions

1. Modular monolith before microservices.
2. Spring MVC before WebFlux because persistence is blocking.
3. Synchronous order processing before asynchronous messaging.
4. PostgreSQL constraints and versioning protect financial invariants.
5. Flyway exclusively owns schema evolution.
6. Records are used for DTOs and events, not JPA entities.
7. Concurrency features are introduced only alongside reproducible tests and observable behavior.
8. Performance changes require measurement.

## 21. Deferred Decisions

The following decisions are intentionally deferred:

- RabbitMQ versus Kafka
- Platform threads versus virtual threads for asynchronous workers
- Optimistic versus pessimistic locking for every high-contention path
- Extraction of modules into microservices
- Caching technology
- External identity provider (authentication is handled by the application for the MVP)
- Cloud hosting platform
- Frontend architecture

Each deferred decision should be made when a concrete requirement or measurement provides enough evidence.

## 22. References

- Spring Boot system requirements: <https://docs.spring.io/spring-boot/system-requirements.html>
- Spring WebFlux overview and concurrency model: <https://docs.spring.io/spring-framework/reference/web/webflux/new-framework.html>
- Flyway versioned migrations: <https://documentation.red-gate.com/flyway/flyway-concepts/migrations/versioned-migrations>
- Flyway repeatable migrations: <https://documentation.red-gate.com/flyway/flyway-concepts/migrations/repeatable-migrations>
- Java 25 virtual threads: <https://docs.oracle.com/en/java/javase/25/core/virtual-threads.html>
- Java 25 records: <https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Record.html>
