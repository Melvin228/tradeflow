CREATE TABLE trade_order (
    id                UUID PRIMARY KEY,
    portfolio_id      UUID NOT NULL,
    instrument_id     UUID NOT NULL,
    side              VARCHAR(4) NOT NULL,
    quantity          NUMERIC(19,8) NOT NULL,
    idempotency_key   VARCHAR(100) NOT NULL,
    request_hash      CHAR(64) NOT NULL,
    status            VARCHAR(12) NOT NULL,
    rejection_code    VARCHAR(50),
    submitted_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    executed_at       TIMESTAMPTZ,

    CONSTRAINT fk_order_portfolio
        FOREIGN KEY (portfolio_id) REFERENCES portfolio (id),
    CONSTRAINT fk_order_instrument
        FOREIGN KEY (instrument_id) REFERENCES instrument (id),
    CONSTRAINT uk_order_portfolio_idempotency UNIQUE (portfolio_id, idempotency_key),
    CONSTRAINT ck_order_side
        CHECK (side IN ('BUY', 'SELL')),
    CONSTRAINT ck_order_quantity_positive
        CHECK (quantity > 0),
    CONSTRAINT ck_order_idempotency_key_non_blank
        CHECK (btrim(idempotency_key) <> ''),
    CONSTRAINT ck_order_request_hash_format
        CHECK (request_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_order_status
        CHECK (status IN ('PENDING', 'EXECUTED', 'REJECTED')),
    CONSTRAINT ck_order_lifecycle
        CHECK (
            (status = 'PENDING'  AND executed_at IS NULL AND rejection_code IS NULL)
         OR (status = 'EXECUTED' AND executed_at IS NOT NULL AND rejection_code IS NULL)
         OR (status = 'REJECTED' AND executed_at IS NULL AND rejection_code IS NOT NULL)
        )
);

CREATE TABLE position (
    id                UUID PRIMARY KEY,
    portfolio_id      UUID NOT NULL,
    instrument_id     UUID NOT NULL,
    quantity          NUMERIC(19,8) NOT NULL DEFAULT 0,
    average_cost      NUMERIC(19,4) NOT NULL DEFAULT 0,
    version           BIGINT NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_position_portfolio
        FOREIGN KEY (portfolio_id) REFERENCES portfolio (id),
    CONSTRAINT fk_position_instrument
        FOREIGN KEY (instrument_id) REFERENCES instrument (id),
    CONSTRAINT uk_position_portfolio_instrument
        UNIQUE (portfolio_id, instrument_id),
    CONSTRAINT ck_position_quantity_non_negative
        CHECK (quantity >= 0),
    CONSTRAINT ck_position_average_cost_non_negative
        CHECK (average_cost >= 0),
    CONSTRAINT ck_position_empty_average_cost
        CHECK (quantity > 0 OR average_cost = 0)
);

CREATE TABLE trade (
    id                UUID PRIMARY KEY,
    order_id          UUID NOT NULL,
    execution_price   NUMERIC(19,4) NOT NULL,
    quantity          NUMERIC(19,8) NOT NULL,
    gross_amount      NUMERIC(19,4) NOT NULL,
    executed_at       TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_trade_order
        FOREIGN KEY (order_id) REFERENCES trade_order (id),
    CONSTRAINT uk_trade_order UNIQUE (order_id),
    CONSTRAINT ck_trade_price_positive
        CHECK (execution_price > 0),
    CONSTRAINT ck_trade_quantity_positive
        CHECK (quantity > 0),
    CONSTRAINT ck_trade_gross_amount_positive
        CHECK (gross_amount > 0)
);

CREATE TABLE cash_ledger_entry (
    id                UUID PRIMARY KEY,
    portfolio_id      UUID NOT NULL,
    order_id          UUID,
    entry_type        VARCHAR(20) NOT NULL,
    amount            NUMERIC(19,4) NOT NULL,
    balance_after     NUMERIC(19,4) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cash_ledger_portfolio
        FOREIGN KEY (portfolio_id) REFERENCES portfolio (id),
    CONSTRAINT fk_cash_ledger_order
        FOREIGN KEY (order_id) REFERENCES trade_order (id),
    CONSTRAINT uk_cash_ledger_order_type UNIQUE (order_id, entry_type),
    CONSTRAINT ck_cash_ledger_entry_type
        CHECK (entry_type IN ('INITIAL_DEPOSIT', 'BUY_DEBIT', 'SELL_CREDIT')),
    CONSTRAINT ck_cash_ledger_balance_non_negative
        CHECK (balance_after >= 0),
    CONSTRAINT ck_cash_ledger_amount_direction
        CHECK (
            (entry_type = 'INITIAL_DEPOSIT' AND order_id IS NULL AND amount >= 0)
         OR (entry_type = 'BUY_DEBIT'       AND order_id IS NOT NULL AND amount < 0)
         OR (entry_type = 'SELL_CREDIT'     AND order_id IS NOT NULL AND amount > 0)
        )
);
