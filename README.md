# TradeFlow

TradeFlow is a simulated portfolio and trade-order processing platform.

The project demonstrates transaction management, concurrency control,
idempotency, asynchronous processing and reliable messaging using Java
and Spring Boot.

No real money, brokerage accounts or live exchange connections are used.

## MVP User Journey

1. Create a portfolio with a simulated cash balance.
2. View available financial instruments.
3. Submit a buy or sell order.
4. Validate the portfolio's cash or holdings.
5. Execute or reject the order.
6. View cash, positions and order history.

## MVP Scope

- Portfolio creation
- Simulated cash balance
- Seeded financial instruments
- Buy orders
- Sell orders
- Position tracking
- Order and trade history
- Transactional order processing
- Idempotency protection
- Concurrent-order protection
- REST API
- PostgreSQL
- Automated tests
- Docker Compose
- Actuator health monitoring

## Non-goals

The first release will not include:

- Real market data
- Real brokerage integration
- Authentication and authorization
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
