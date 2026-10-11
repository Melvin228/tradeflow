CREATE INDEX ix_refresh_token_user_active
    ON auth_refresh_token (user_id, expires_at)
    WHERE revoked_at IS NULL;

CREATE INDEX ix_portfolio_user_created
    ON portfolio (user_id, created_at DESC);

CREATE INDEX ix_instrument_active_symbol
    ON instrument (symbol)
    WHERE active = TRUE;

CREATE INDEX ix_order_portfolio_submitted
    ON trade_order (portfolio_id, submitted_at DESC);

CREATE INDEX ix_order_instrument_submitted
    ON trade_order (instrument_id, submitted_at DESC);

CREATE INDEX ix_cash_ledger_portfolio_created
    ON cash_ledger_entry (portfolio_id, created_at DESC);
