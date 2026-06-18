-- ============================================================
-- DỮ LIỆU MẪU (MOCK DATA) CHO HỆ THỐNG BLUEMOON
-- Chạy script này sau khi đã tạo xong bảng và Trigger/FK
-- ============================================================

USE CNPM; -- Hãy thay đổi tên Database nếu bạn dùng tên khác
GO

-- ----------------------------------------------------------------
-- 1. THÊM NHÂN VIÊN & CHI TIẾT NHÂN VIÊN (5 Staff)
-- ----------------------------------------------------------------
INSERT INTO dbo.staff (staff_id, password, role) VALUES
        ('STF_01', 'hashed_pw_123', 'ADMIN'),
('STF_02', 'hashed_pw_123', 'MANAGER_FINANCE'),
('STF_03', 'hashed_pw_123', 'MANAGER_RESIDENT'),
('STF_04', 'hashed_pw_123', 'FINANCE'),
('STF_05', 'hashed_pw_123', 'RESIDENT');

INSERT INTO dbo.staff_detail (staff_id, name, birthday, address) VALUES
('STF_01', N'Vũ Văn Quản Trị', '1990-01-01', N'Hà Nội'),
('STF_02', N'Trần Thị Tài Chính', '1992-05-12', N'Hà Nội'),
('STF_03', N'Lê Văn Dân Cư', '1988-08-20', N'Hà Nội'),
('STF_04', N'Phạm Thu Kế Toán', '1995-11-05', N'Hà Nội'),
('STF_05', N'Nguyễn Cư Dân', '1998-02-15', N'Hà Nội');
GO

-- ----------------------------------------------------------------
-- 2. THÊM CĂN HỘ (15 Căn hộ)
-- ----------------------------------------------------------------
INSERT INTO dbo.apartment (apart_id, house_id, room_number, area, description) VALUES
('AP_01', 'HS_01', '101', 65.5, N'Căn hộ tiêu chuẩn'),
('AP_02', 'HS_02', '102', 65.5, N'Căn hộ tiêu chuẩn'),
('AP_03', 'HS_03', '103', 70.0, N'Căn hộ góc'),
('AP_04', 'HS_04', '104', 65.5, N'Căn hộ tiêu chuẩn'),
('AP_05', 'HS_05', '201', 65.5, N'Căn hộ tiêu chuẩn'),
('AP_06', 'HS_06', '202', 65.5, N'Căn hộ tiêu chuẩn'),
('AP_07', 'HS_07', '203', 70.0, N'Căn hộ góc'),
('AP_08', 'HS_08', '204', 65.5, N'Căn hộ tiêu chuẩn'),
('AP_09', 'HS_09', '301', 65.5, N'Căn hộ tiêu chuẩn'),
('AP_10', 'HS_10', '302', 65.5, N'Căn hộ tiêu chuẩn'),
('AP_11', 'HS_11', '303', 70.0, N'Căn hộ góc'),
('AP_12', 'HS_12', '304', 65.5, N'Căn hộ tiêu chuẩn'),
('AP_13', 'HS_13', '401', 65.5, N'Căn hộ tiêu chuẩn'),
('AP_14', 'HS_14', '402', 65.5, N'Căn hộ tiêu chuẩn'),
('AP_15', 'HS_15', '403', 75.0, N'Penthouse mini');
GO

-- ----------------------------------------------------------------
-- 3. THÊM HỘ KHẨU (15 Hộ khẩu tương ứng)
-- ----------------------------------------------------------------
INSERT INTO dbo.house_reg (house_id, apart_id) VALUES
('HS_01', 'AP_01'), ('HS_02', 'AP_02'), ('HS_03', 'AP_03'),
('HS_04', 'AP_04'), ('HS_05', 'AP_05'), ('HS_06', 'AP_06'),
('HS_07', 'AP_07'), ('HS_08', 'AP_08'), ('HS_09', 'AP_09'),
('HS_10', 'AP_10'), ('HS_11', 'AP_11'), ('HS_12', 'AP_12'),
('HS_13', 'AP_13'), ('HS_14', 'AP_14'), ('HS_15', 'AP_15');
GO

-- ----------------------------------------------------------------
-- 4. THÊM CƯ DÂN (40 Cư dân)
-- ----------------------------------------------------------------
-- Dùng một vòng lặp nhỏ hoặc viết trực tiếp để đảm bảo tính minh bạch.
-- Dưới đây là 40 records trực tiếp.
INSERT INTO dbo.resident (resident_id, name, telephone, birthday) VALUES
('RES_01', N'Vũ Văn 1',  '0900000001', '1980-01-01'), ('RES_02', N'Vũ Văn 2',  '0900000002', '1981-01-01'),
('RES_03', N'Vũ Văn 3',  '0900000003', '1982-01-01'), ('RES_04', N'Vũ Văn 4',  '0900000004', '1983-01-01'),
('RES_05', N'Vũ Văn 5',  '0900000005', '1984-01-01'), ('RES_06', N'Vũ Văn 6',  '0900000006', '1985-01-01'),
('RES_07', N'Vũ Văn 7',  '0900000007', '1986-01-01'), ('RES_08', N'Vũ Văn 8',  '0900000008', '1987-01-01'),
('RES_09', N'Vũ Văn 9',  '0900000009', '1988-01-01'), ('RES_10', N'Vũ Văn 10', '0900000010', '1989-01-01'),
('RES_11', N'Vũ Văn 11', '0900000011', '1990-01-01'), ('RES_12', N'Vũ Văn 12', '0900000012', '1991-01-01'),
('RES_13', N'Vũ Văn 13', '0900000013', '1992-01-01'), ('RES_14', N'Vũ Văn 14', '0900000014', '1993-01-01'),
('RES_15', N'Vũ Văn 15', '0900000015', '1994-01-01'), ('RES_16', N'Vũ Văn 16', '0900000016', '1995-01-01'),
('RES_17', N'Vũ Văn 17', '0900000017', '1996-01-01'), ('RES_18', N'Vũ Văn 18', '0900000018', '1997-01-01'),
('RES_19', N'Vũ Văn 19', '0900000019', '1998-01-01'), ('RES_20', N'Vũ Văn 20', '0900000020', '1999-01-01'),
('RES_21', N'Vũ Văn 21', '0900000021', '2000-01-01'), ('RES_22', N'Vũ Văn 22', '0900000022', '2001-01-01'),
('RES_23', N'Vũ Văn 23', '0900000023', '2002-01-01'), ('RES_24', N'Vũ Văn 24', '0900000024', '2003-01-01'),
('RES_25', N'Vũ Văn 25', '0900000025', '2004-01-01'), ('RES_26', N'Vũ Văn 26', '0900000026', '2005-01-01'),
('RES_27', N'Vũ Văn 27', '0900000027', '2006-01-01'), ('RES_28', N'Vũ Văn 28', '0900000028', '2007-01-01'),
('RES_29', N'Vũ Văn 29', '0900000029', '2008-01-01'), ('RES_30', N'Vũ Văn 30', '0900000030', '2009-01-01'),
('RES_31', N'Vũ Văn 31', '0900000031', '2010-01-01'), ('RES_32', N'Vũ Văn 32', '0900000032', '2011-01-01'),
('RES_33', N'Vũ Văn 33', '0900000033', '2012-01-01'), ('RES_34', N'Vũ Văn 34', '0900000034', '2013-01-01'),
('RES_35', N'Vũ Văn 35', '0900000035', '2014-01-01'), ('RES_36', N'Vũ Văn 36', '0900000036', '2015-01-01'),
('RES_37', N'Vũ Văn 37', '0900000037', '2016-01-01'), ('RES_38', N'Vũ Văn 38', '0900000038', '2017-01-01'),
('RES_39', N'Vũ Văn 39', '0900000039', '2018-01-01'), ('RES_40', N'Vũ Văn 40', '0900000040', '2019-01-01');
GO

-- ----------------------------------------------------------------
-- 5. PHÂN BỔ CƯ DÂN VÀO CĂN HỘ (Master = 1 cho 15 người đầu tiên)
-- ----------------------------------------------------------------
-- Set 15 Chủ hộ
INSERT INTO dbo.resident_house (resident_id, house_id, isMaster) VALUES
('RES_01', 'HS_01', 1), ('RES_02', 'HS_02', 1), ('RES_03', 'HS_03', 1),
('RES_04', 'HS_04', 1), ('RES_05', 'HS_05', 1), ('RES_06', 'HS_06', 1),
('RES_07', 'HS_07', 1), ('RES_08', 'HS_08', 1), ('RES_09', 'HS_09', 1),
('RES_10', 'HS_10', 1), ('RES_11', 'HS_11', 1), ('RES_12', 'HS_12', 1),
('RES_13', 'HS_13', 1), ('RES_14', 'HS_14', 1), ('RES_15', 'HS_15', 1);

-- Phân bổ 25 thành viên phụ thuộc còn lại (isMaster = 0)
INSERT INTO dbo.resident_house (resident_id, house_id, isMaster) VALUES
('RES_16', 'HS_01', 0), ('RES_17', 'HS_01', 0), ('RES_18', 'HS_02', 0),
('RES_19', 'HS_02', 0), ('RES_20', 'HS_03', 0), ('RES_21', 'HS_03', 0),
('RES_22', 'HS_04', 0), ('RES_23', 'HS_04', 0), ('RES_24', 'HS_05', 0),
('RES_25', 'HS_05', 0), ('RES_26', 'HS_06', 0), ('RES_27', 'HS_06', 0),
('RES_28', 'HS_07', 0), ('RES_29', 'HS_07', 0), ('RES_30', 'HS_08', 0),
('RES_31', 'HS_08', 0), ('RES_32', 'HS_09', 0), ('RES_33', 'HS_09', 0),
('RES_34', 'HS_10', 0), ('RES_35', 'HS_10', 0), ('RES_36', 'HS_11', 0),
('RES_37', 'HS_12', 0), ('RES_38', 'HS_13', 0), ('RES_39', 'HS_14', 0),
('RES_40', 'HS_15', 0);
GO

-- ----------------------------------------------------------------
-- 6. THIẾT LẬP DANH MỤC THU (Receivable)
-- ----------------------------------------------------------------
-- 1 chữ điện = 3500 VNĐ | 1 khối nước = 15000 VNĐ
INSERT INTO dbo.receivable (recei_id, recei_name, mandatory, fixed, price, description) VALUES
('FEE_ELEC',  N'Tiền điện sinh hoạt', 1, 0, 3500,  N'Đơn giá 1 số điện'),
('FEE_WATER', N'Tiền nước sinh hoạt', 1, 0, 15000, N'Đơn giá 1 m3 nước');
GO

-- ----------------------------------------------------------------
-- 7. TẠO HÓA ĐƠN THU TIỀN CHO CÁC HỘ (House_Recei)
-- ----------------------------------------------------------------
DECLARE @deadline DATETIMEOFFSET = DATEADD(DAY, 15,SYSDATETIMEOFFSET()); -- Hạn chót sau 15 ngày
DECLARE @payDate  DATETIMEOFFSET = SYSDATETIMEOFFSET();      -- Ngày thanh toán

-- TẠO HÓA ĐƠN TIỀN ĐIỆN (Giả sử số lượng số điện dùng là 120 số)
INSERT INTO dbo.house_recei (house_id, recei_id, status, quantity, pay_date, pay_deadline, description) VALUES
('HS_01', 'FEE_ELEC', 1, 120, @payDate, @deadline, N'Tháng 6/2026'),
('HS_02', 'FEE_ELEC', 1, 150, @payDate, @deadline, N'Tháng 6/2026'),
('HS_03', 'FEE_ELEC', 1, 110, @payDate, @deadline, N'Tháng 6/2026'),
('HS_04', 'FEE_ELEC', 1, 200, @payDate, @deadline, N'Tháng 6/2026'),
('HS_05', 'FEE_ELEC', 1, 90,  @payDate, @deadline, N'Tháng 6/2026'),
('HS_06', 'FEE_ELEC', 1, 130, @payDate, @deadline, N'Tháng 6/2026'),
('HS_07', 'FEE_ELEC', 0, 140, NULL,     @deadline, N'Tháng 6/2026'),
('HS_08', 'FEE_ELEC', 0, 180, NULL,     @deadline, N'Tháng 6/2026'),
('HS_09', 'FEE_ELEC', 0, 105, NULL,     @deadline, N'Tháng 6/2026'),
('HS_10', 'FEE_ELEC', 0, 125, NULL,     @deadline, N'Tháng 6/2026'),
('HS_11', 'FEE_ELEC', 0, 160, NULL,     @deadline, N'Tháng 6/2026'),
('HS_12', 'FEE_ELEC', 0, 175, NULL,     @deadline, N'Tháng 6/2026'),
('HS_13', 'FEE_ELEC', 0, 95,  NULL,     @deadline, N'Tháng 6/2026'),
('HS_14', 'FEE_ELEC', 0, 145, NULL,     @deadline, N'Tháng 6/2026'),
('HS_15', 'FEE_ELEC', 0, 210, NULL,     @deadline, N'Tháng 6/2026');

-- TẠO HÓA ĐƠN TIỀN NƯỚC (Giả sử số lượng m3 dùng là 15 m3)
INSERT INTO dbo.house_recei (house_id, recei_id, status, quantity, pay_date, pay_deadline, description) VALUES
('HS_01', 'FEE_WATER', 1, 15, @payDate, @deadline, N'Tháng 6/2026'),
('HS_02', 'FEE_WATER', 1, 18, @payDate, @deadline, N'Tháng 6/2026'),
('HS_03', 'FEE_WATER', 1, 12, @payDate, @deadline, N'Tháng 6/2026'),
('HS_04', 'FEE_WATER', 1, 20, @payDate, @deadline, N'Tháng 6/2026'),
('HS_05', 'FEE_WATER', 0, 14, NULL,     @deadline, N'Tháng 6/2026'),
('HS_06', 'FEE_WATER', 0, 16, NULL,     @deadline, N'Tháng 6/2026'),
('HS_07', 'FEE_WATER', 1, 22, @payDate, @deadline, N'Tháng 6/2026'),
('HS_08', 'FEE_WATER', 1, 19, @payDate, @deadline, N'Tháng 6/2026'),
('HS_09', 'FEE_WATER', 0, 13, NULL,     @deadline, N'Tháng 6/2026'),
('HS_10', 'FEE_WATER', 0, 15, NULL,     @deadline, N'Tháng 6/2026'),
('HS_11', 'FEE_WATER', 0, 17, NULL,     @deadline, N'Tháng 6/2026'),
('HS_12', 'FEE_WATER', 0, 11, NULL,     @deadline, N'Tháng 6/2026'),
('HS_13', 'FEE_WATER', 0, 14, NULL,     @deadline, N'Tháng 6/2026'),
('HS_14', 'FEE_WATER', 0, 21, NULL,     @deadline, N'Tháng 6/2026'),
('HS_15', 'FEE_WATER', 0, 25, NULL,     @deadline, N'Tháng 6/2026');
GO