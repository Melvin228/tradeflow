# TradeFlow Backlog

## Milestone 1: Project Bootstrap

- TF-001 Generate Spring Boot backend
- TF-002 Configure PostgreSQL
- TF-003 Configure Flyway
- TF-004 Add Actuator health endpoint
- TF-005 Add Docker Compose

## Milestone 2: Portfolio and Instruments

- TF-006 Create portfolio schema
- TF-007 Implement portfolio creation
- TF-008 Implement portfolio retrieval
- TF-009 Create instrument schema
- TF-010 Seed financial instruments
- TF-011 Implement instrument listing

## Milestone 3: Synchronous Trading

- TF-012 Create order, position and trade schemas
- TF-013 Implement buy-order validation
- TF-014 Implement transactional buy execution
- TF-015 Implement sell-order validation
- TF-016 Implement transactional sell execution
- TF-017 Implement order retrieval
- TF-018 Write integration tests

## Milestone 4: Concurrency and Idempotency

- TF-019 Add idempotency key
- TF-020 Write concurrent withdrawal test
- TF-021 Reproduce lost-update problem
- TF-022 Add optimistic locking
- TF-023 Handle concurrent update conflicts
- TF-024 Verify cash and positions never become negative

## Future Milestones

- Asynchronous order processing
- Bounded thread pool
- RabbitMQ or Kafka
- Retry and dead-letter queue
- Transactional outbox
- React dashboard
- Deployment
