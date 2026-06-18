-- ============================================================
--  BLUEMOON APARTMENT MANAGEMENT — MS SQL Server Schema
--  Target: Local SQL Server (LOCAL)
-- ============================================================
CREATE DATABASE CNPM
USE CNPM

-- ----------------------------------------------------------------
-- 1. TABLE CREATION
-- ----------------------------------------------------------------

IF OBJECT_ID('dbo.house_recei',    'U') IS NULL
CREATE TABLE dbo.house_recei (
    house_id     NVARCHAR(20)  NOT NULL,
    recei_id     NVARCHAR(20)  NOT NULL,
    status       BIT           NOT NULL DEFAULT 0,
    quantity     BIGINT        NOT NULL DEFAULT 1,
    pay_date     DATETIMEOFFSET,
    pay_deadline DATETIMEOFFSET NOT NULL,
    description  NVARCHAR(MAX),
    CONSTRAINT PK_house_recei PRIMARY KEY (house_id, recei_id)
);
GO

IF OBJECT_ID('dbo.resident_house', 'U') IS NULL
CREATE TABLE dbo.resident_house (
    resident_id  NVARCHAR(20) NOT NULL,
    house_id     NVARCHAR(20) NOT NULL,
    isMaster     BIT          NOT NULL DEFAULT 0,
    CONSTRAINT PK_resident_house PRIMARY KEY (resident_id, house_id)
);
GO

IF OBJECT_ID('dbo.house_reg',   'U') IS NULL
CREATE TABLE dbo.house_reg (
    house_id   NVARCHAR(20)  PRIMARY KEY,
    apart_id   NVARCHAR(20)  NOT NULL,
    created_at DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    delete_at  DATETIMEOFFSET
);
GO

IF OBJECT_ID('dbo.apartment',   'U') IS NULL
CREATE TABLE dbo.apartment (
    apart_id    NVARCHAR(20)  PRIMARY KEY,
    house_id    NVARCHAR(20)  NOT NULL,
    room_number NVARCHAR(20)  NOT NULL,
    area        FLOAT         NOT NULL,
    description NVARCHAR(MAX),
    delete_at   DATETIMEOFFSET
);
GO

IF OBJECT_ID('dbo.receivable',  'U') IS NULL
CREATE TABLE dbo.receivable (
    recei_id    NVARCHAR(20)  PRIMARY KEY,
    recei_name  NVARCHAR(255) NOT NULL,
    mandatory   BIT           NOT NULL DEFAULT 0,
    fixed       BIT           NOT NULL DEFAULT 0,
    price       BIGINT        NOT NULL DEFAULT 0,
    description NVARCHAR(MAX),
    created_at  DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    delete_at   DATETIMEOFFSET
);
GO

IF OBJECT_ID('dbo.resident',    'U') IS NULL
CREATE TABLE dbo.resident (
    resident_id NVARCHAR(20)   PRIMARY KEY,
    name        NVARCHAR(255)  NOT NULL,
    birthday    DATE,
    telephone   NVARCHAR(20)   UNIQUE,
    created_at  DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    delete_at   DATETIMEOFFSET
);
GO

IF OBJECT_ID('dbo.staff',       'U') IS NULL
CREATE TABLE dbo.staff (
    staff_id   NVARCHAR(20)   PRIMARY KEY,
    password   NVARCHAR(255)  NOT NULL,
    role       NVARCHAR(50)   NOT NULL,
    created_at DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    delete_at  DATETIMEOFFSET
);
GO

IF OBJECT_ID('dbo.staff_detail','U') IS NULL
CREATE TABLE dbo.staff_detail (
    staff_id   NVARCHAR(20)   PRIMARY KEY,
    name       NVARCHAR(255)  NOT NULL,
    birthday   DATE           NOT NULL,
    address    NVARCHAR(MAX),
    created_at DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    CONSTRAINT FK_staff_detail_staff
        FOREIGN KEY (staff_id) REFERENCES dbo.staff(staff_id) ON DELETE CASCADE
);
GO

-- ----------------------------------------------------------------
-- 2. FOREIGN KEYS (added after all tables exist)
-- ----------------------------------------------------------------

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_house_reg_apartment'
)
    ALTER TABLE dbo.house_reg
        ADD CONSTRAINT FK_house_reg_apartment
            FOREIGN KEY (apart_id) REFERENCES dbo.apartment(apart_id);
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_resident_house_resident'
)
    ALTER TABLE dbo.resident_house
        ADD CONSTRAINT FK_resident_house_resident
            FOREIGN KEY (resident_id) REFERENCES dbo.resident(resident_id);
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_resident_house_house_reg'
)
    ALTER TABLE dbo.resident_house
        ADD CONSTRAINT FK_resident_house_house_reg
            FOREIGN KEY (house_id) REFERENCES dbo.house_reg(house_id);
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_house_recei_house_reg'
)
    ALTER TABLE dbo.house_recei
        ADD CONSTRAINT FK_house_recei_house_reg
            FOREIGN KEY (house_id) REFERENCES dbo.house_reg(house_id);
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_house_recei_receivable'
)
    ALTER TABLE dbo.house_recei
        ADD CONSTRAINT FK_house_recei_receivable
            FOREIGN KEY (recei_id) REFERENCES dbo.receivable(recei_id);
GO

-- ----------------------------------------------------------------
-- 3. INDEXES
-- ----------------------------------------------------------------

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_staff_delete_at')
    CREATE INDEX idx_staff_delete_at      ON dbo.staff(delete_at);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_resident_delete_at')
    CREATE INDEX idx_resident_delete_at   ON dbo.resident(delete_at);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_house_reg_delete_at')
    CREATE INDEX idx_house_reg_delete_at  ON dbo.house_reg(delete_at);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_receivable_delete_at')
    CREATE INDEX idx_receivable_delete_at ON dbo.receivable(delete_at);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_house_recei_status')
    CREATE INDEX idx_house_recei_status   ON dbo.house_recei(status);
GO

-- ----------------------------------------------------------------
-- 4. SOFT-DELETE TRIGGERS
--    INSTEAD OF DELETE → sets delete_at = SYSDATETIMEOFFSET()
-- ----------------------------------------------------------------

-- staff
CREATE OR ALTER TRIGGER trg_soft_delete_staff
ON dbo.staff
INSTEAD OF DELETE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.staff
    SET    delete_at = SYSDATETIMEOFFSET()
    WHERE  staff_id IN (SELECT staff_id FROM DELETED);
END;
GO

-- resident
CREATE OR ALTER TRIGGER trg_soft_delete_resident
ON dbo.resident
INSTEAD OF DELETE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.resident
    SET    delete_at = SYSDATETIMEOFFSET()
    WHERE  resident_id IN (SELECT resident_id FROM DELETED);
END;
GO

-- house_reg
CREATE OR ALTER TRIGGER trg_soft_delete_house_reg
ON dbo.house_reg
INSTEAD OF DELETE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.house_reg
    SET    delete_at = SYSDATETIMEOFFSET()
    WHERE  house_id IN (SELECT house_id FROM DELETED);
END;
GO

-- receivable
CREATE OR ALTER TRIGGER trg_soft_delete_receivable
ON dbo.receivable
INSTEAD OF DELETE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.receivable
    SET    delete_at = SYSDATETIMEOFFSET()
    WHERE  recei_id IN (SELECT recei_id FROM DELETED);
END;
GO

-- ----------------------------------------------------------------
-- 5. HARD-DELETE STORED PROCEDURE FOR RESIDENT
--    Purges residents where delete_at IS NOT NULL
--    AND no unpaid house_recei rows remain (status = 0).
-- ----------------------------------------------------------------

CREATE OR ALTER PROCEDURE dbo.sp_hard_delete_residents
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @purged INT;

    -- 5a. Remove resident_house links for eligible residents
    DELETE rh
    FROM   dbo.resident_house rh
    WHERE  rh.resident_id IN (
        SELECT r.resident_id
        FROM   dbo.resident r
        WHERE  r.delete_at IS NOT NULL
          AND  NOT EXISTS (
              SELECT 1
              FROM   dbo.resident_house rh2
              JOIN   dbo.house_recei    hc  ON hc.house_id = rh2.house_id
              WHERE  rh2.resident_id = r.resident_id
                AND  hc.status       = 0          -- 0 = unpaid (BIT false)
          )
    );

    -- 5b. Hard-delete the resident rows
    DELETE FROM dbo.resident
    WHERE delete_at IS NOT NULL
      AND NOT EXISTS (
          SELECT 1
          FROM   dbo.resident_house rh
          JOIN   dbo.house_recei    hc ON hc.house_id = rh.house_id
          WHERE  rh.resident_id = dbo.resident.resident_id
            AND  hc.status      = 0
      );

    SET @purged = @@ROWCOUNT;
    PRINT CONCAT('[sp_hard_delete_residents] Purged ', @purged,
                 ' resident(s) at ', CONVERT(NVARCHAR, SYSDATETIMEOFFSET(), 127));
END;
GO

-- ----------------------------------------------------------------
-- 6. SQL SERVER AGENT JOB — runs sp_hard_delete_residents monthly
--    (Requires SQL Server Agent; run once by a sysadmin)
-- ----------------------------------------------------------------

-- Uncomment and execute in SSMS as sysadmin:
/*
USE msdb;
GO

EXEC sp_add_job
    @job_name = N'BlueMoon_HardDelete_Residents_Monthly';

EXEC sp_add_jobstep
    @job_name   = N'BlueMoon_HardDelete_Residents_Monthly',
    @step_name  = N'Purge soft-deleted residents',
    @command    = N'EXEC dbo.sp_hard_delete_residents;',
    @database_name = N'BlueMoon';   -- <-- change to your DB name

EXEC sp_add_schedule
    @schedule_name      = N'Monthly_1st_02AM',
    @freq_type          = 16,       -- monthly
    @freq_interval      = 1,        -- day 1 of month
    @active_start_time  = 20000;    -- 02:00:00

EXEC sp_attach_schedule
    @job_name      = N'BlueMoon_HardDelete_Residents_Monthly',
    @schedule_name = N'Monthly_1st_02AM';

EXEC sp_add_jobserver
    @job_name = N'BlueMoon_HardDelete_Residents_Monthly';
GO
*/
