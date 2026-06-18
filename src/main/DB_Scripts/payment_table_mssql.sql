-- ============================================================
-- PAYMENT TABLE FOR FINANCE HISTORY — MSSQL
-- ============================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'payment')
BEGIN
    CREATE TABLE payment (
        payment_id    VARCHAR(20)   PRIMARY KEY,
        house_id      VARCHAR(50)   NOT NULL REFERENCES house_reg(house_id),
        recei_id      VARCHAR(50)   NOT NULL REFERENCES receivable(recei_id),
        amount_paid   BIGINT        NOT NULL,
        payment_date  DATETIMEOFFSET NOT NULL,
        payment_method VARCHAR(50),
        transaction_id VARCHAR(100),
        notes         NVARCHAR(MAX),
        created_at    DATETIMEOFFSET NOT NULL DEFAULT GETUTCDATE(),
        FOREIGN KEY (house_id, recei_id) REFERENCES house_recei(house_id, recei_id)
    );

    CREATE INDEX idx_payment_house_id ON payment(house_id);
    CREATE INDEX idx_payment_recei_id ON payment(recei_id);
    CREATE INDEX idx_payment_payment_date ON payment(payment_date);
END;
