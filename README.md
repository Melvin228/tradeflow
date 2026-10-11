# TradeFlow

TradeFlow is a simulated portfolio and trade-order processing platform with
user accounts and authentication.

The project demonstrates modular application design, secure credential
handling, transaction management, concurrency control, and idempotent order
processing using Java and Spring Boot. Asynchronous processing and reliable
messaging are future extensions, not part of the initial release.

No real money, brokerage accounts or live exchange connections are used.

## MVP User Journey

1. Register an account and log in.
2. Create a portfolio with a simulated cash balance.
3. View available financial instruments.
4. Submit a buy or sell order.
5. Validate the portfolio's cash or holdings.
6. Execute or reject the order.
7. View cash, positions and order history.

## MVP Scope

- User registration and login
- Refresh-token rotation and logout
- Ownership-scoped portfolio access
- Portfolio creation
- Simulated cash balance
- Seeded financial instruments
- Buy orders
- Sell orders
- Position tracking
- Order and trade history
- Transactional order processing
- Order idempotency protection
- Concurrent-order protection
- REST API
- PostgreSQL
- Automated tests
- Docker Compose
- Actuator health monitoring

## Non-goals for the initial release

The first release will not include:

- Password reset, email verification or multi-factor authentication
- Real market data
- Real brokerage integration
- Multiple currencies
- Options, futures or cryptocurrency
- Full exchange matching
- Mobile application
- Kubernetes
- Microservices
- Advanced frontend

## Technology Stack

- Java 25
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- JUnit
- Testcontainers
- Docker Compose
- Spring Boot Actuator
- OpenAPI

## Definition of Done

Version 0.1 is complete when:

- The application can start using Docker Compose.
- A user can register, log in, refresh credentials and log out.
- Users cannot access another user's portfolios or orders.
- A portfolio can be created.
- Instruments can be listed.
- Buy and sell orders can be submitted.
- Cash and positions update transactionally.
- Invalid orders are rejected.
- Duplicate requests cannot execute twice.
- Concurrent requests cannot corrupt balances.
- Automated tests pass.
- API documentation is available.
- The README explains how to run the application.
