package services;

import models.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import repository.HouseReceiRepo;
import repository.PaymentRepo;
import repository.ResidentHouseRepo;
import repository.ResidentRepo;
import user.AuthManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service xuất dữ liệu sang file Excel (.xlsx) bằng poi-ooxml.
 *
 * Bao gồm 2 nghiệp vụ:
 *  1. Xuất danh sách hóa đơn ĐÃ ĐÓNG (status = true) của một hộ khẩu (house_id),
 *     kèm thông tin lần thanh toán (payment).
 *  2. Xuất danh sách toàn bộ cư dân (resident) hiện có.
 *
 * Quy tắc bắt buộc: chỉ MANAGER hoặc ADMIN mới được xuất báo cáo (tránh lộ
 * thông tin tài chính / cá nhân cho RESIDENT thường). Mọi lần xuất đều được
 * ghi log qua AuditLogService.
 */
public class ExportServices {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final HouseReceiRepo houseReceiRepo;
    private final PaymentRepo paymentRepo;
    private final ResidentRepo residentRepo;
    private final ResidentHouseRepo residentHouseRepo;

    public ExportServices(HouseReceiRepo houseReceiRepo,
                          PaymentRepo paymentRepo,
                          ResidentRepo residentRepo,
                          ResidentHouseRepo residentHouseRepo) {
        this.houseReceiRepo = houseReceiRepo;
        this.paymentRepo = paymentRepo;
        this.residentRepo = residentRepo;
        this.residentHouseRepo = residentHouseRepo;
    }

    // ══════════════════════════════════════════════════════════════
    // 1) XUẤT HÓA ĐƠN ĐÃ ĐÓNG CỦA MỘT HỘ
    // ══════════════════════════════════════════════════════════════

    /**
     * Xuất toàn bộ hóa đơn (khoản thu) ĐÃ ĐÓNG (status = true) của một hộ khẩu
     * ra file Excel.
     *
     * Mỗi dòng gồm: Tên khoản thu, Loại (mandatory/optional), Số lượng (điện/nước),
     * Số tiền, Ngày lập hạn đóng (pay_deadline), Ngày thanh toán (pay_date),
     * Số tiền đã trả (từ Payment), Phương thức thanh toán, Ghi chú.
     *
     * Quyền: chỉ MANAGER hoặc ADMIN.
     *
     * Đầu vào:
     *   houseId    — mã hộ khẩu cần xuất
     *   outputPath — đường dẫn file .xlsx sẽ được tạo (ví dụ: "/tmp/hoadon_HK001.xlsx")
     *
     * Đầu ra: số lượng dòng hóa đơn đã xuất (không tính header)
     *
     * Ném ra: SecurityException nếu không có quyền
     *         IllegalArgumentException nếu houseId không hợp lệ
     *         IOException nếu lỗi khi viết file
     */
    public int exportPaidInvoicesOfHouse(String houseId, String outputPath) throws IOException {
        if (!AuthManager.hasManagerRole()) {
            throw new SecurityException("Bạn không có quyền xuất báo cáo hóa đơn (chỉ MANAGER/ADMIN).");
        }
        if (houseId == null || houseId.isBlank()) {
            throw new IllegalArgumentException("Mã hộ khẩu (houseId) không được để trống.");
        }

        // Lấy toàn bộ khoản thu của hộ, lọc các khoản đã đóng (status = true)
        List<HouseRecei> all = houseReceiRepo.findByHouseId(houseId);
        List<HouseRecei> paid = all.stream().filter(HouseRecei::isStatus).toList();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Hóa đơn đã đóng");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

            // ── Tiêu đề báo cáo ──
            int rowIdx = 0;
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH SÁCH HÓA ĐƠN ĐÃ ĐÓNG — HỘ KHẨU: " + houseId);
            titleCell.setCellStyle(headerStyle);

            rowIdx++; // dòng trống

            // ── Header ──
            String[] headers = {
                    "STT", "Mã khoản thu", "Tên khoản tiền", "Loại khoản thu",
                    "Số lượng đã dùng", "Số tiền (VND)", "Hạn đóng",
                    "Ngày thanh toán", "Số tiền đã trả (VND)", "Phương thức TT", "Ghi chú"
            };
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Dữ liệu ──
            int stt = 1;
            long totalPaid = 0;
            for (HouseRecei hr : paid) {
                Receivable receivable = hr.getReceivable();

                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(receivable != null ? receivable.getReceiId() : "");
                row.createCell(2).setCellValue(receivable != null ? receivable.getReceiName() : "");
                row.createCell(3).setCellValue(receivable != null
                        ? (receivable.isMandatory() ? "Bắt buộc" : "Đóng góp") : "");
                row.createCell(4).setCellValue(hr.getQuantity());

                Cell priceCell = row.createCell(5);
                priceCell.setCellValue(receivable != null ? receivable.getPrice() : 0);
                priceCell.setCellStyle(moneyStyle);

                row.createCell(6).setCellValue(hr.getPayDeadline() != null
                        ? DATE_FMT.format(hr.getPayDeadline()) : "");

                // ✅ FIX: Lấy lịch sử thanh toán của khoản thu này
                List<Payment> payments = paymentRepo.findByHouseRecei(hr);
                long amountPaidSum = payments.stream().mapToLong(Payment::getAmountPaid).sum();
                totalPaid += amountPaidSum;

                // ✅ FIX: Lấy payment mới nhất (phần tử cuối cùng trong list)
                Payment lastPayment = getLatestPayment(payments);
                String lastPaymentDate = lastPayment == null ? ""
                        : DATETIME_FMT.format(lastPayment.getPaymentDate());
                String lastPaymentMethod = lastPayment == null ? ""
                        : nullToEmpty(lastPayment.getPaymentMethod());
                String notes = lastPayment == null ? ""
                        : nullToEmpty(lastPayment.getNotes());

                row.createCell(7).setCellValue(
                        hr.getPayDate() != null ? DATETIME_FMT.format(hr.getPayDate()) : lastPaymentDate);

                Cell paidCell = row.createCell(8);
                paidCell.setCellValue(amountPaidSum);
                paidCell.setCellStyle(moneyStyle);

                row.createCell(9).setCellValue(lastPaymentMethod);
                row.createCell(10).setCellValue(notes);
            }

            // ── Dòng tổng cộng ──
            Row totalRow = sheet.createRow(rowIdx++);
            totalRow.createCell(4).setCellValue("Tổng đã thu:");
            Cell totalCell = totalRow.createCell(5);
            totalCell.setCellValue(totalPaid);
            totalCell.setCellStyle(moneyStyle);

            // Auto-size cột
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }

            AuditLogService.log("HouseRecei", houseId, "EXPORT",
                    "Xuất danh sách hóa đơn đã đóng của hộ " + houseId
                            + " (" + paid.size() + " hóa đơn) ra Excel: " + outputPath);

            return paid.size();
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 2) XUẤT DANH SÁCH CƯ DÂN
    // ══════════════════════════════════════════════════════════════

    /**
     * Xuất toàn bộ danh sách cư dân (chưa bị xóa) ra file Excel.
     * Mỗi dòng gồm: Mã cư dân, Họ tên, Ngày sinh, SĐT, Hộ khẩu đang ở,
     * Vai trò trong hộ (Chủ hộ / Thành viên), Ngày tạo.
     *
     * Quyền: chỉ MANAGER hoặc ADMIN.
     *
     * Đầu vào:
     *   outputPath — đường dẫn file .xlsx sẽ được tạo
     *
     * Đầu ra: số lượng cư dân đã xuất
     *
     * Ném ra: SecurityException nếu không có quyền
     *         IOException nếu lỗi khi viết file
     */
    public int exportResidentList(String outputPath) throws IOException {
        if (!AuthManager.hasManagerRole()) {
            throw new SecurityException("Bạn không có quyền xuất danh sách cư dân (chỉ MANAGER/ADMIN).");
        }

        List<Resident> residents = residentRepo.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách cư dân");

            CellStyle headerStyle = createHeaderStyle(workbook);

            int rowIdx = 0;
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH SÁCH CƯ DÂN — Tổng số: " + residents.size());
            titleCell.setCellStyle(headerStyle);

            rowIdx++; // dòng trống

            String[] headers = {
                    "STT", "Mã cư dân", "Họ và tên", "Ngày sinh", "Số điện thoại",
                    "Hộ khẩu đang ở", "Vai trò trong hộ", "Ngày đăng ký"
            };
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int stt = 1;
            for (Resident resident : residents) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(resident.getResidentId());
                row.createCell(2).setCellValue(resident.getName());
                row.createCell(3).setCellValue(resident.getBirthday() != null
                        ? resident.getBirthday().toString() : "");
                row.createCell(4).setCellValue(nullToEmpty(resident.getTelephone()));

                // Tìm hộ khẩu hiện tại của cư dân (nếu có)
                List<ResidentHouse> houses = residentHouseRepo.findByResidentId(resident.getResidentId());
                String houseId = "";
                String role = "";
                if (!houses.isEmpty()) {
                    ResidentHouse rh = houses.get(0);
                    houseId = rh.getHouseReg() != null ? rh.getHouseReg().getHouseId() : "";
                    role = rh.isMaster() ? "Chủ hộ" : "Thành viên";
                }
                row.createCell(5).setCellValue(houseId);
                row.createCell(6).setCellValue(role);

                row.createCell(7).setCellValue(resident.getCreatedAt() != null
                        ? DATE_FMT.format(resident.getCreatedAt()) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }

            AuditLogService.log("Resident", "ALL", "EXPORT",
                    "Xuất danh sách cư dân (" + residents.size() + " người) ra Excel: " + outputPath);

            return residents.size();
        }
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════

    /**
     * ✅ FIX: Helper method để lấy payment mới nhất từ danh sách.
     * Giả định payments được sort từ cũ → mới, nên payment mới nhất ở cuối list.
     *
     * Đầu vào: payments - danh sách thanh toán
     * Đầu ra: Payment mới nhất, hoặc null nếu list rỗng
     */
    private Payment getLatestPayment(List<Payment> payments) {
        return payments.isEmpty() ? null : payments.get(payments.size() - 1);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
