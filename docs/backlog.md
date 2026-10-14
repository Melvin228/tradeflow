# TradeFlow Backlog

## Milestone 1: Project Bootstrap

- TF-001 Generate Spring Boot backend
- TF-002 Configure PostgreSQL
- TF-003 Configure Flyway
- TF-004 Add Actuator health endpoint
- TF-005 Add Docker Compose

## Milestone 2: Users and Authentication

- TF-006 Create userEntity and refresh-token schema
- TF-007 Implement userEntity registration with normalized email and adaptive password hashing
- TF-008 Implement login and issue access and refresh tokens
- TF-009 Implement refresh-token rotation and logout
- TF-010 Add authentication and userEntity integration tests

## Milestone 3: Portfolio and Instruments

- TF-011 Create portfolio schema
- TF-012 Implement portfolio creation for the authenticated userEntity
- TF-013 Implement ownership-scoped portfolio retrieval
- TF-014 Create instrument schema
- TF-015 Seed financial instruments
- TF-016 Implement instrument listing

## Milestone 4: Synchronous Trading

- TF-017 Create order, position and trade schemas
- TF-018 Implement buy-order validation
- TF-019 Implement transactional buy execution
- TF-020 Implement sell-order validation
- TF-021 Implement transactional sell execution
- TF-022 Implement ownership-scoped order retrieval
- TF-023 Write PostgreSQL integration tests

## Milestone 5: Concurrency and Idempotency

- TF-024 Add idempotency key
- TF-025 Write concurrent order-execution test
- TF-026 Reproduce lost-update problem
- TF-027 Add optimistic locking
- TF-028 Handle concurrent update conflicts
- TF-029 Verify cash and positions never become negative

## Future Milestones

- Password reset, email verification, and multi-factor authentication
- Asynchronous order processing
- Bounded thread pool
- RabbitMQ or Kafka
- Retry and dead-letter queue
- Transactional outbox
- React dashboard
- Deployment
