ALTER TABLE spending_summary DROP CONSTRAINT uq_spending_summary;
ALTER TABLE spending_summary DROP COLUMN source_id;
ALTER TABLE spending_summary ADD CONSTRAINT uq_spending_summary UNIQUE (period, currency, category);

DROP INDEX IF EXISTS idx_spending_summary_source_period;
CREATE INDEX idx_spending_summary_period ON spending_summary (period);
