CREATE TABLE spending_transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id        UUID         NOT NULL,
    external_id     VARCHAR(255) NOT NULL UNIQUE,
    source_id       VARCHAR(255) NOT NULL,
    source_type     VARCHAR(50),
    amount          NUMERIC(19, 4) NOT NULL,
    currency        VARCHAR(10)  NOT NULL,
    merchant_name   VARCHAR(255),
    description     TEXT,
    transaction_type VARCHAR(50),
    transacted_at   TIMESTAMPTZ  NOT NULL,
    received_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_spending_transactions_source_id ON spending_transactions (source_id);
CREATE INDEX idx_spending_transactions_transacted_at ON spending_transactions (transacted_at);
CREATE INDEX idx_spending_transactions_merchant ON spending_transactions (merchant_name);
