-- ============================================================
-- PAYMENT TABLE FOR FINANCE HISTORY — PostgreSQL
-- ============================================================

CREATE TABLE IF NOT EXISTS payment (
    payment_id    TEXT        PRIMARY KEY,
    house_id      TEXT        NOT NULL REFERENCES house_reg(house_id)   ON DELETE RESTRICT,
    recei_id      TEXT        NOT NULL REFERENCES receivable(recei_id)  ON DELETE RESTRICT,
    amount_paid   BIGINT      NOT NULL,
    payment_date  TIMESTAMPTZ NOT NULL,
    payment_method TEXT,
    transaction_id TEXT,
    notes         TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (house_id, recei_id) REFERENCES house_recei(house_id, recei_id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_payment_house_id     ON payment(house_id);
CREATE INDEX IF NOT EXISTS idx_payment_recei_id     ON payment(recei_id);
CREATE INDEX IF NOT EXISTS idx_payment_payment_date ON payment(payment_date);
