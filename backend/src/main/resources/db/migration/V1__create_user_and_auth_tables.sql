CREATE TABLE app_user (
    id              UUID PRIMARY KEY,
    email           VARCHAR(320) NOT NULL,
    display_name    VARCHAR(120) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version         BIGINT NOT NULL DEFAULT 0,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_app_user_email UNIQUE (email),
    CONSTRAINT ck_app_user_email_normalized
        CHECK (email = lower(btrim(email)) AND btrim(email) <> ''),
    CONSTRAINT ck_app_user_display_name_non_blank
        CHECK (btrim(display_name) <> ''),
    CONSTRAINT ck_app_user_password_hash_non_blank
        CHECK (btrim(password_hash) <> ''),
    CONSTRAINT ck_app_user_status
        CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED'))
);

CREATE TABLE auth_refresh_token (
    id                      UUID PRIMARY KEY,
    user_id                 UUID NOT NULL,
    token_hash              CHAR(64) NOT NULL,
    expires_at              TIMESTAMPTZ NOT NULL,
    revoked_at              TIMESTAMPTZ,
    replaced_by_token_id    UUID,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_refresh_token_replacement
        FOREIGN KEY (replaced_by_token_id) REFERENCES auth_refresh_token (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT uk_refresh_token_replacement UNIQUE (replaced_by_token_id),
    CONSTRAINT ck_refresh_token_hash_format
        CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_refresh_token_revocation_time
        CHECK (revoked_at IS NULL OR revoked_at >= created_at)
);
