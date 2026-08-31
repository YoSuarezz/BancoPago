CREATE TABLE transfer (
    id                    UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    source_account_number VARCHAR(20)   NOT NULL,
    target_account_number VARCHAR(20)   NOT NULL,
    amount                DECIMAL(15,2) NOT NULL,
    currency              VARCHAR(3)    NOT NULL DEFAULT 'COP',
    status                VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    description           VARCHAR(200),
    idempotency_key       VARCHAR(64)   NOT NULL UNIQUE,
    version               BIGINT        NOT NULL DEFAULT 0,
    created_at            TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT chk_transfer_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_transfer_different_accounts CHECK (source_account_number <> target_account_number)
);

CREATE INDEX idx_transfer_source     ON transfer(source_account_number);
CREATE INDEX idx_transfer_target     ON transfer(target_account_number);
CREATE INDEX idx_transfer_created_at ON transfer(created_at DESC);
