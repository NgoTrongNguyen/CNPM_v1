package services;

import models.*;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import repository.HouseRegRepo;
import repository.ResidentHouseRepo;
import user.AuthManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service xuất thông tin Hộ khẩu (house_reg) sang file Word (.docx) bằng poi-ooxml.
 *
 * Nội dung file xuất gồm:
 *  - Thông tin hộ khẩu: mã hộ khẩu, ngày đăng ký.
 *  - Thông tin căn hộ đang sống: mã căn hộ, số phòng, diện tích, mô tả.
 *  - Danh sách thành viên: mã cư dân, họ tên, ngày sinh, SĐT, vai trò (chủ hộ/thành viên).
 *
 * Quyền: chỉ MANAGER hoặc ADMIN mới được xuất.
 * Mọi lần xuất đều được ghi log qua AuditLogService.
 */
public class HouseRegExportServices {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final HouseRegRepo houseRegRepo;
    private final ResidentHouseRepo residentHouseRepo;

    public HouseRegExportServices(HouseRegRepo houseRegRepo, ResidentHouseRepo residentHouseRepo) {
        this.houseRegRepo = houseRegRepo;
        this.residentHouseRepo = residentHouseRepo;
    }

    /**
     * Xuất thông tin chi tiết của một hộ khẩu (gồm thông tin căn hộ và danh sách
     * thành viên) ra file Word (.docx).
     *
     * Đầu vào:
     *   houseId    — mã hộ khẩu cần xuất
     *   outputPath — đường dẫn file .docx sẽ được tạo (ví dụ: "/tmp/ho_khau_HK001.docx")
     *
     * Đầu ra: không có (file được viết ra outputPath)
     *
     * Ném ra: SecurityException nếu không có quyền (chỉ MANAGER/ADMIN)
     *         IllegalArgumentException nếu không tìm thấy hộ khẩu
     *         IOException nếu lỗi khi viết file
     */
    public void exportHouseRegDetail(String houseId, String outputPath) throws IOException {
        if (!AuthManager.hasManagerRole()) {
            throw new SecurityException("Bạn không có quyền xuất thông tin hộ khẩu (chỉ MANAGER/ADMIN).");
        }

        HouseReg houseReg = houseRegRepo.findByHouseId(houseId);
        if (houseReg == null) {
            throw new IllegalArgumentException("Không tìm thấy hộ khẩu: " + houseId);
        }

        Apartment apartment = houseReg.getApartment();
        List<ResidentHouse> members = residentHouseRepo.findByHouseId(houseId);

        try (XWPFDocument doc = new XWPFDocument()) {

            // ── Tiêu đề ──
            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("THÔNG TIN HỘ KHẨU");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            XWPFParagraph subTitle = doc.createParagraph();
            subTitle.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun subRun = subTitle.createRun();
            subRun.setText("Mã hộ khẩu: " + houseReg.getHouseId());
            subRun.setItalic(true);

            addEmptyParagraph(doc);

            // ── Thông tin hộ khẩu ──
            addHeading(doc, "1. Thông tin hộ khẩu");
            addKeyValue(doc, "Mã hộ khẩu", houseReg.getHouseId());
            addKeyValue(doc, "Ngày đăng ký", houseReg.getCreateAt() != null
                    ? DATE_FMT.format(houseReg.getCreateAt()) : "—");

            addEmptyParagraph(doc);

            // ── Thông tin căn hộ đang sống ──
            addHeading(doc, "2. Thông tin căn hộ đang sống");
            if (apartment != null) {
                addKeyValue(doc, "Mã căn hộ", apartment.getApartId());
                addKeyValue(doc, "Số phòng", apartment.getRoomNumber());
                addKeyValue(doc, "Diện tích (m²)", String.valueOf(apartment.getArea()));
                addKeyValue(doc, "Mô tả", apartment.getDescription() != null
                        ? apartment.getDescription() : "—");
            } else {
                addKeyValue(doc, "Căn hộ", "Chưa được gán căn hộ");
            }

            addEmptyParagraph(doc);

            // ── Danh sách thành viên ──
            addHeading(doc, "3. Danh sách thành viên (" + members.size() + " người)");

            // ✅ FIX: Tạo table với đủ hàng cho header + members
            XWPFTable table = doc.createTable(members.size() + 1, 5);
            table.setWidth("100%");

            String[] headers = {"STT", "Mã cư dân", "Họ và tên", "Ngày sinh", "Vai trò"};
            XWPFTableRow headerRow = table.getRow(0);
            for (int i = 0; i < headers.length; i++) {
                XWPFTableCell cell = headerRow.getCell(i);
                cell.setText("");
                XWPFParagraph p = cell.getParagraphs().get(0);
                XWPFRun run = p.createRun();
                run.setText(headers[i]);
                run.setBold(true);
            }

            // ✅ FIX: Dùng index loop thay vì stt để tránh nhầm lẫn
            for (int idx = 0; idx < members.size(); idx++) {
                ResidentHouse rh = members.get(idx);
                Resident resident = rh.getResident();

                // Row index = idx + 1 (vì row 0 là header)
                XWPFTableRow row = table.getRow(idx + 1);

                row.getCell(0).setText(String.valueOf(idx + 1));  // STT (1-based)
                row.getCell(1).setText(resident != null ? resident.getResidentId() : "");
                row.getCell(2).setText(resident != null ? resident.getName() : "");
                row.getCell(3).setText(resident != null && resident.getBirthday() != null
                        ? resident.getBirthday().toString() : "");
                row.getCell(4).setText(rh.isMaster() ? "Chủ hộ" : "Thành viên");
            }

            addEmptyParagraph(doc);

            // ── Chân trang ──
            XWPFParagraph footer = doc.createParagraph();
            footer.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun footerRun = footer.createRun();
            footerRun.setItalic(true);
            footerRun.setFontSize(10);
            footerRun.setText("Xuất bởi hệ thống BlueMoon — "
                    + DATE_FMT.format(java.time.Instant.now()));

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                doc.write(fos);
            }
        }

        AuditLogService.log("HouseReg", houseId, "EXPORT",
                "Xuất thông tin hộ khẩu " + houseId + " ra Word: " + outputPath);
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════

    private void addHeading(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(13);
    }

    private void addKeyValue(XWPFDocument doc, String key, String value) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun runKey = p.createRun();
        runKey.setText(key + ": ");
        runKey.setBold(true);

        XWPFRun runValue = p.createRun();
        runValue.setText(value != null ? value : "—");
    }

    private void addEmptyParagraph(XWPFDocument doc) {
        doc.createParagraph();
    }
}
