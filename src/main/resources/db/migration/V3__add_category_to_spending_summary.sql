ALTER TABLE spending_summary DROP CONSTRAINT uq_spending_summary;
ALTER TABLE spending_summary DROP COLUMN merchant_name;
ALTER TABLE spending_summary ADD COLUMN category VARCHAR(50) NOT NULL DEFAULT 'OTHER';
ALTER TABLE spending_summary ADD CONSTRAINT uq_spending_summary UNIQUE (source_id, period, currency, category);
