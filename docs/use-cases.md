# Use Cases

## UC-001: Create Portfolio

A user creates a portfolio with a name and initial simulated cash balance.

### Acceptance Criteria for UC-001

- Portfolio name is required.
- Initial cash cannot be negative.
- A unique portfolio ID is generated.
- The created portfolio is returned.

## UC-002: View Instruments

A user views the financial instruments available for simulated trading.

### Acceptance Criteria for UC-002

- The application contains seeded instruments.
- Each instrument has a symbol, name and price.
- All active instruments are returned.

## UC-003: Submit Buy Order

A user submits an order to purchase an instrument.

### Acceptance Criteria for UC-003

- Portfolio and instrument must exist.
- Quantity must be positive.
- Portfolio must have sufficient cash.
- Duplicate idempotency keys must not create another order.
- Successful execution reduces cash and increases the position.
- Cash must never become negative.

## UC-004: Submit Sell Order

A user submits an order to sell an instrument.

### Acceptance Criteria for UC-004

- Portfolio and instrument must exist.
- Quantity must be positive.
- Portfolio must own enough units.
- Duplicate idempotency keys must not create another order.
- Successful execution increases cash and reduces the position.
- Position quantity must never become negative.
