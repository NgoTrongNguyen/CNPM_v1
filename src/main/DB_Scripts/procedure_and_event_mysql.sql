USE CNPM;

-- ----------------------------------------------------------------
-- HARD-DELETE STORED PROCEDURE FOR RESIDENT
-- ----------------------------------------------------------------
DELIMITER //

DROP PROCEDURE IF EXISTS sp_hard_delete_residents //
CREATE PROCEDURE sp_hard_delete_residents()
BEGIN
    -- Xóa liên kết resident_house của các cư dân đủ điều kiện
    DELETE rh FROM resident_house rh
    INNER JOIN resident r ON rh.resident_id = r.resident_id
    WHERE r.delete_at IS NOT NULL
      AND NOT EXISTS (
          SELECT 1 FROM house_recei hc
          INNER JOIN resident_house rh2 ON hc.house_id = rh2.house_id
          WHERE rh2.resident_id = r.resident_id
            AND hc.status = 0 -- 0 = chưa thanh toán
      );

    -- Xóa vĩnh viễn cư dân
    DELETE r FROM resident r
    WHERE r.delete_at IS NOT NULL
      AND NOT EXISTS (
          SELECT 1 FROM resident_house rh
          INNER JOIN house_recei hc ON hc.house_id = rh.house_id
          WHERE rh.resident_id = r.resident_id
            AND hc.status = 0
      );

SELECT CONCAT('[sp_hard_delete_residents] Executed at ', NOW()) AS log_message;
END //

DELIMITER ;

-- ----------------------------------------------------------------
-- MYSQL EVENT SCHEDULER (Chạy thay thế SQL Server Agent)
-- Chạy tự động vào mùng 1 hàng tháng lúc 02:00 AM
-- ----------------------------------------------------------------
SET GLOBAL event_scheduler = ON;

CREATE EVENT IF NOT EXISTS BlueMoon_HardDelete_Residents_Monthly
ON SCHEDULE EVERY 1 MONTH
STARTS CURRENT_DATE + INTERVAL 1 MONTH - INTERVAL (DAY(CURRENT_DATE) - 1) DAY + INTERVAL 2 HOUR
DO
    CALL sp_hard_delete_residents();