-- ============================================================
-- BLUEMOON APARTMENT MANAGEMENT — MySQL Schema
-- ============================================================
CREATE DATABASE IF NOT EXISTS CNPM CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE CNPM;

-- ----------------------------------------------------------------
-- 1. TABLE CREATION
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS house_recei (
                                           house_id     VARCHAR(20)  NOT NULL,
    recei_id     VARCHAR(20)  NOT NULL,
    status       BOOLEAN      NOT NULL DEFAULT FALSE,
    quantity     BIGINT       NOT NULL DEFAULT 1,
    pay_date     DATETIME,
    pay_deadline DATETIME     NOT NULL,
    description  TEXT,
    PRIMARY KEY (house_id, recei_id)
    );

CREATE TABLE IF NOT EXISTS resident_house (
                                              resident_id  VARCHAR(20) NOT NULL,
    house_id     VARCHAR(20) NOT NULL,
    isMaster     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (resident_id, house_id)
    );

CREATE TABLE IF NOT EXISTS house_reg (
                                         house_id   VARCHAR(20) PRIMARY KEY,
    apart_id   VARCHAR(20) NOT NULL,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delete_at  DATETIME
    );

CREATE TABLE IF NOT EXISTS apartment (
                                         apart_id    VARCHAR(20) PRIMARY KEY,
    house_id    VARCHAR(20) NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    area        FLOAT       NOT NULL,
    description TEXT,
    delete_at   DATETIME
    );

CREATE TABLE IF NOT EXISTS receivable (
                                          recei_id    VARCHAR(20)  PRIMARY KEY,
    recei_name  VARCHAR(255) NOT NULL,
    mandatory   BOOLEAN      NOT NULL DEFAULT FALSE,
    fixed       BOOLEAN      NOT NULL DEFAULT FALSE,
    price       BIGINT       NOT NULL DEFAULT 0,
    description TEXT,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delete_at   DATETIME
    );

CREATE TABLE IF NOT EXISTS resident (
                                        resident_id VARCHAR(20)  PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    birthday    DATE,
    telephone   VARCHAR(20)  UNIQUE,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delete_at   DATETIME
    );

CREATE TABLE IF NOT EXISTS staff (
                                     staff_id   VARCHAR(20)  PRIMARY KEY,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delete_at  DATETIME
    );

CREATE TABLE IF NOT EXISTS staff_detail (
                                            staff_id   VARCHAR(20)  PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    birthday   DATE         NOT NULL,
    address    TEXT,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_staff_detail_staff
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS payment (
                                       payment_id    VARCHAR(20)   PRIMARY KEY,
    house_id      VARCHAR(50)   NOT NULL,
    recei_id      VARCHAR(50)   NOT NULL,
    amount_paid   BIGINT        NOT NULL,
    payment_date  DATETIME      NOT NULL,
    payment_method VARCHAR(50),
    transaction_id VARCHAR(100),
    notes         TEXT,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- ----------------------------------------------------------------
-- 2. FOREIGN KEYS
-- ----------------------------------------------------------------
ALTER TABLE house_reg
    ADD CONSTRAINT FK_house_reg_apartment FOREIGN KEY (apart_id) REFERENCES apartment(apart_id);

ALTER TABLE resident_house
    ADD CONSTRAINT FK_resident_house_resident FOREIGN KEY (resident_id) REFERENCES resident(resident_id),
    ADD CONSTRAINT FK_resident_house_house_reg FOREIGN KEY (house_id) REFERENCES house_reg(house_id);

ALTER TABLE house_recei
    ADD CONSTRAINT FK_house_recei_house_reg FOREIGN KEY (house_id) REFERENCES house_reg(house_id),
    ADD CONSTRAINT FK_house_recei_receivable FOREIGN KEY (recei_id) REFERENCES receivable(recei_id);

ALTER TABLE payment
    ADD CONSTRAINT fk_payment_house_reg FOREIGN KEY (house_id) REFERENCES house_reg(house_id),
    ADD CONSTRAINT fk_payment_receivable FOREIGN KEY (recei_id) REFERENCES receivable(recei_id),
    ADD CONSTRAINT fk_payment_house_recei FOREIGN KEY (house_id, recei_id) REFERENCES house_recei(house_id, recei_id);

-- ----------------------------------------------------------------
-- 3. INDEXES
-- ----------------------------------------------------------------
CREATE INDEX idx_staff_delete_at      ON staff(delete_at);
CREATE INDEX idx_resident_delete_at   ON resident(delete_at);
CREATE INDEX idx_house_reg_delete_at  ON house_reg(delete_at);
CREATE INDEX idx_receivable_delete_at ON receivable(delete_at);
CREATE INDEX idx_house_recei_status   ON house_recei(status);
CREATE INDEX idx_payment_house_id     ON payment(house_id);
CREATE INDEX idx_payment_recei_id     ON payment(recei_id);
CREATE INDEX idx_payment_payment_date ON payment(payment_date);