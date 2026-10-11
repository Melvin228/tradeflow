CREATE TABLE portfolio (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL,
    name            VARCHAR(120) NOT NULL,
    currency        CHAR(3) NOT NULL DEFAULT 'USD',
    cash_balance    NUMERIC(19,4) NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_portfolio_user
        FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT uk_portfolio_user_name UNIQUE (user_id, name),
    CONSTRAINT ck_portfolio_name_non_blank
        CHECK (btrim(name) <> ''),
    CONSTRAINT ck_portfolio_currency_uppercase
        CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_portfolio_cash_non_negative
        CHECK (cash_balance >= 0)
);

CREATE TABLE instrument (
    id              UUID PRIMARY KEY,
    symbol          VARCHAR(20) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    currency        CHAR(3) NOT NULL DEFAULT 'USD',
    current_price   NUMERIC(19,4) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_instrument_symbol UNIQUE (symbol),
    CONSTRAINT ck_instrument_symbol_format
        CHECK (symbol ~ '^[A-Z0-9][A-Z0-9.-]{0,19}$'),
    CONSTRAINT ck_instrument_name_non_blank
        CHECK (btrim(name) <> ''),
    CONSTRAINT ck_instrument_currency_uppercase
        CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_instrument_price_positive
        CHECK (current_price > 0)
);
