# Architecture Decisions

## ADR-001: Modular Monolith

TradeFlow will begin as a modular monolith.

Reason:

- Easier to develop and test
- Avoids premature distributed-system complexity
- Modules can be extracted later if justified

## ADR-002: PostgreSQL

PostgreSQL will store portfolios, orders, positions and trades.

Reason:

- Transactions and consistency are important
- Supports locking and constraints
- Suitable for financial-style relational data

## ADR-003: Synchronous Processing First

The first release will process orders synchronously.

Reason:

- Establish correct business rules first
- Make transaction boundaries easier to understand
- Add concurrency and messaging only after correctness is proven
