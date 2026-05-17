CREATE TABLE spending_summary (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_id           VARCHAR(255) NOT NULL,
    period              VARCHAR(7)   NOT NULL,
    merchant_name       VARCHAR(255) NOT NULL DEFAULT 'UNKNOWN',
    currency            VARCHAR(10)  NOT NULL,
    total_amount        NUMERIC(19, 4) NOT NULL DEFAULT 0,
    transaction_count   INT          NOT NULL DEFAULT 0,
    last_updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_spending_summary UNIQUE (source_id, period, currency, merchant_name)
);

CREATE INDEX idx_spending_summary_source_period ON spending_summary (source_id, period);
