-- ============================================================
--  BLUEMOON APARTMENT MANAGEMENT — PostgreSQL Schema
--  Target: Supabase (CLOUD) / any PostgreSQL instance
-- ============================================================

-- ----------------------------------------------------------------
-- 1. TABLE CREATION
-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS staff (
    staff_id   TEXT        PRIMARY KEY,
    password   TEXT        NOT NULL,
    role       TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    delete_at  TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS staff_detail (
    staff_id   TEXT        PRIMARY KEY REFERENCES staff(staff_id) ON DELETE CASCADE,
    name       TEXT        NOT NULL,
    birthday   DATE        NOT NULL,
    address    TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS resident (
    resident_id TEXT        PRIMARY KEY,
    name        TEXT        NOT NULL,
    birthday    DATE,
    telephone   TEXT        UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    delete_at   TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS apartment (
    apart_id    TEXT          PRIMARY KEY,
    house_id    TEXT          NOT NULL,
    room_number TEXT          NOT NULL,
    area        FLOAT8        NOT NULL,
    description TEXT,
    delete_at   TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS house_reg (
    house_id   TEXT        PRIMARY KEY,
    apart_id   TEXT        NOT NULL REFERENCES apartment(apart_id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    delete_at  TIMESTAMPTZ
);

-- resident_house (mapped as ResidentHouse in code)
CREATE TABLE IF NOT EXISTS resident_house (
    resident_id TEXT    NOT NULL REFERENCES resident(resident_id)  ON DELETE RESTRICT,
    house_id    TEXT    NOT NULL REFERENCES house_reg(house_id)    ON DELETE RESTRICT,
    "isMaster"  BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (resident_id, house_id)
);

CREATE TABLE IF NOT EXISTS receivable (
    recei_id    TEXT        PRIMARY KEY,
    recei_name  TEXT        NOT NULL,
    mandatory   BOOLEAN     NOT NULL DEFAULT FALSE,
    fixed       BOOLEAN     NOT NULL DEFAULT FALSE,
    price       BIGINT      NOT NULL DEFAULT 0,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    delete_at   TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS house_recei (
    house_id     TEXT        NOT NULL REFERENCES house_reg(house_id)   ON DELETE RESTRICT,
    recei_id     TEXT        NOT NULL REFERENCES receivable(recei_id)  ON DELETE RESTRICT,
    status       BOOLEAN     NOT NULL DEFAULT FALSE,
    quantity     BIGINT      NOT NULL DEFAULT 1,
    pay_date     TIMESTAMPTZ,
    pay_deadline TIMESTAMPTZ NOT NULL,
    description  TEXT,
    PRIMARY KEY (house_id, recei_id)
);

-- ----------------------------------------------------------------
-- 2. INDEXES
-- ----------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_staff_delete_at         ON staff(delete_at);
CREATE INDEX IF NOT EXISTS idx_resident_delete_at      ON resident(delete_at);
CREATE INDEX IF NOT EXISTS idx_house_reg_delete_at     ON house_reg(delete_at);
CREATE INDEX IF NOT EXISTS idx_receivable_delete_at    ON receivable(delete_at);
CREATE INDEX IF NOT EXISTS idx_house_recei_status      ON house_recei(status);
CREATE INDEX IF NOT EXISTS idx_resident_house_house    ON resident_house(house_id);

-- ----------------------------------------------------------------
-- 3. SOFT-DELETE TRIGGERS
--    Sets delete_at = NOW() instead of physically removing rows.
--    Tables: staff, resident, house_reg, receivable
-- ----------------------------------------------------------------

-- Helper function used by all soft-delete triggers
CREATE OR REPLACE FUNCTION fn_soft_delete()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    -- Redirect DELETE → UPDATE delete_at = NOW()
    EXECUTE format(
        'UPDATE %I SET delete_at = NOW() WHERE %I = $1',
        TG_TABLE_NAME,
        TG_ARGV[0]   -- primary key column passed as trigger argument
    ) USING OLD;     -- pass OLD row's PK value

    RETURN NULL;     -- cancel the original DELETE
END;
$$;

-- staff soft-delete
DROP TRIGGER IF EXISTS trg_soft_delete_staff ON staff;
CREATE TRIGGER trg_soft_delete_staff
    BEFORE DELETE ON staff
    FOR EACH ROW
    EXECUTE FUNCTION fn_soft_delete('staff_id');

-- resident soft-delete
DROP TRIGGER IF EXISTS trg_soft_delete_resident ON resident;
CREATE TRIGGER trg_soft_delete_resident
    BEFORE DELETE ON resident
    FOR EACH ROW
    EXECUTE FUNCTION fn_soft_delete('resident_id');

-- house_reg soft-delete
DROP TRIGGER IF EXISTS trg_soft_delete_house_reg ON house_reg;
CREATE TRIGGER trg_soft_delete_house_reg
    BEFORE DELETE ON house_reg
    FOR EACH ROW
    EXECUTE FUNCTION fn_soft_delete('house_id');

-- receivable soft-delete
DROP TRIGGER IF EXISTS trg_soft_delete_receivable ON receivable;
CREATE TRIGGER trg_soft_delete_receivable
    BEFORE DELETE ON receivable
    FOR EACH ROW
    EXECUTE FUNCTION fn_soft_delete('recei_id');

-- ----------------------------------------------------------------
-- 4. HARD-DELETE JOB FOR RESIDENT
--    Permanently removes residents whose delete_at IS NOT NULL
--    AND who have no unpaid house_recei rows (status = false).
--    Scheduled monthly via pg_cron (Supabase supports pg_cron).
-- ----------------------------------------------------------------

-- Step 4a: The purge function
CREATE OR REPLACE FUNCTION fn_hard_delete_residents()
RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    v_count INT;
BEGIN
    -- Delete resident_house links for eligible residents first
    DELETE FROM resident_house
    WHERE resident_id IN (
        SELECT r.resident_id
        FROM   resident r
        WHERE  r.delete_at IS NOT NULL
          AND  NOT EXISTS (
              -- resident still linked to a house_reg with unpaid fees
              SELECT 1
              FROM   resident_house rh
              JOIN   house_recei    hc ON hc.house_id = rh.house_id
              WHERE  rh.resident_id = r.resident_id
                AND  hc.status      = FALSE       -- FALSE = unpaid
          )
    );

    -- Now hard-delete the residents themselves
    DELETE FROM resident
    WHERE delete_at IS NOT NULL
      AND NOT EXISTS (
          SELECT 1
          FROM   resident_house rh
          JOIN   house_recei    hc ON hc.house_id = rh.house_id
          WHERE  rh.resident_id = resident.resident_id
            AND  hc.status      = FALSE
      );

    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '[fn_hard_delete_residents] Purged % resident(s) at %', v_count, NOW();
END;
$$;

-- Step 4b: Schedule via pg_cron (runs on the 1st of every month at 02:00 UTC)
--          Requires pg_cron extension — already available on Supabase.
--          Run this block once manually (or via Supabase SQL editor):
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_extension WHERE extname = 'pg_cron'
    ) THEN
        PERFORM cron.schedule(
            'hard_delete_residents_monthly',   -- job name (unique)
            '0 2 1 * *',                       -- cron: 02:00 on day-1 each month
            'SELECT fn_hard_delete_residents();'
        );
        RAISE NOTICE 'pg_cron job scheduled.';
    ELSE
        RAISE NOTICE 'pg_cron not available — schedule fn_hard_delete_residents() manually.';
    END IF;
END;
$$;
