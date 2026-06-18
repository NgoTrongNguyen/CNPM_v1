package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.Receivable;
import repository.ReceivableRepo;
import services.ReceivableServices;
import services.ReceivableServices.ReceiCategory;
import user.AuthManager;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Controller cho form "Thêm khoản thu mới".
 *
 * Chỉ hiển thị / cho phép submit nếu user hiện tại có quyền MANAGER hoặc ADMIN
 * (kiểm tra qua AuthManager.hasManagerRole()).
 *
 * Form hỗ trợ 4 loại khoản thu:
 *  - SERVICE_ELECTRIC : nhập "số điện đã dùng" -> tự tính tiền theo 3.500 VND/số
 *  - SERVICE_WATER    : nhập "số nước đã dùng (m3)" -> tự tính tiền theo 15.000 VND/m3
 *  - SERVICE_OTHER    : nhập trực tiếp số tiền
 *  - CONTRIBUTION     : nhập trực tiếp số tiền (khoản đóng góp)
 *
 * Hạn đóng (pay_deadline) hiển thị mặc định = ngày lập + 10 ngày (read-only,
 * tính tự động bởi ReceivableServices).
 */
public class ReceivableFormController extends BaseController {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final ReceivableServices service = new ReceivableServices(new ReceivableRepo());

    @FXML private TextField receiIdField;
    @FXML private TextField receiNameField;

    @FXML private ComboBox<ReceiCategory> categoryCombo;

    @FXML private CheckBox mandatoryCheck;

    /** Trường nhập "số đã sử dụng" — hiện khi chọn Điện/Nước */
    @FXML private TextField quantityField;
    @FXML private Label     quantityUnitLabel;

    /** Trường nhập số tiền trực tiếp — hiện khi chọn Dịch vụ khác / Đóng góp */
    @FXML private TextField amountField;

    @FXML private TextField descriptionField;

    /** Label hiển thị giá tiền dự kiến (preview), tính realtime */
    @FXML private Label previewPriceLabel;

    /** Label hiển thị hạn đóng mặc định (ngày lập + 10 ngày) */
    @FXML private Label deadlineLabel;

    @FXML private Label infoLabel;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Receivable result; // null nếu người dùng huỷ

    @FXML
    public void initialize() {
        // ── Kiểm tra quyền ngay khi mở form ──
        if (!AuthManager.hasManagerRole()) {
            infoLabel.setText("Bạn không có quyền thêm khoản thu mới.");
            saveButton.setDisable(true);
        }

        // ── Khởi tạo combo loại khoản thu ──
        categoryCombo.getItems().setAll(
                ReceiCategory.SERVICE_ELECTRIC,
                ReceiCategory.SERVICE_WATER,
                ReceiCategory.SERVICE_OTHER,
                ReceiCategory.CONTRIBUTION
        );
        categoryCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(ReceiCategory c) {
                if (c == null) return "";
                return switch (c) {
                    case SERVICE_ELECTRIC -> "Dịch vụ điện";
                    case SERVICE_WATER -> "Dịch vụ nước";
                    case SERVICE_OTHER -> "Dịch vụ khác";
                    case CONTRIBUTION -> "Đóng góp";
                };
            }

            @Override
            public ReceiCategory fromString(String s) {
                return null; // không cần parse ngược, chỉ chọn từ list
            }
        });

        // ── Hạn đóng mặc định = hôm nay + 10 ngày ──
        Instant deadline = service.calculateDefaultDeadline();
        deadlineLabel.setText(DT_FMT.format(deadline)
                + "  (mặc định: ngày lập + " + ReceivableServices.DEFAULT_PAY_DEADLINE_DAYS + " ngày)");

        // ── Sự kiện thay đổi loại khoản thu -> cập nhật UI tương ứng ──
        categoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateFieldsVisibility(newVal));

        // ── Tính giá tiền realtime khi nhập số/tiền ──
        quantityField.textProperty().addListener((obs, o, n) -> updatePricePreview());
        amountField.textProperty().addListener((obs, o, n) -> updatePricePreview());

        // Mặc định chọn "Dịch vụ điện"
        categoryCombo.getSelectionModel().selectFirst();
    }

    /**
     * Hiện/ẩn các trường nhập tương ứng theo loại khoản thu đã chọn.
     * Đầu vào: category — loại khoản thu hiện được chọn
     * Đầu ra: không có (chỉ thay đổi UI)
     */
    private void updateFieldsVisibility(ReceiCategory category) {
        boolean isUtility = (category == ReceiCategory.SERVICE_ELECTRIC
                || category == ReceiCategory.SERVICE_WATER);

        // ✅ FIX: Clear field không cần để tránh lỗi tính toán
        if (!isUtility) {
            quantityField.clear();
        } else {
            amountField.clear();
        }

        quantityField.setVisible(isUtility);
        quantityField.setManaged(isUtility);
        quantityUnitLabel.setVisible(isUtility);
        quantityUnitLabel.setManaged(isUtility);

        amountField.setVisible(!isUtility);
        amountField.setManaged(!isUtility);

        if (category == ReceiCategory.SERVICE_ELECTRIC) {
            quantityUnitLabel.setText("số điện (kWh) — đơn giá "
                    + String.format("%,d", ReceivableServices.DEFAULT_ELECTRIC_PRICE_PER_UNIT) + " VND/số");
        } else if (category == ReceiCategory.SERVICE_WATER) {
            quantityUnitLabel.setText("số nước (m3) — đơn giá "
                    + String.format("%,d", ReceivableServices.DEFAULT_WATER_PRICE_PER_UNIT) + " VND/m3");
        }

        // Khoản đóng góp mặc định không bắt buộc; dịch vụ mặc định bắt buộc
        mandatoryCheck.setSelected(category != ReceiCategory.CONTRIBUTION);

        updatePricePreview();
    }

    /**
     * Tính lại và hiển thị giá tiền dự kiến dựa trên dữ liệu đang nhập.
     * Đầu vào: không có (đọc trực tiếp từ các TextField)
     * Đầu ra: không có (chỉ cập nhật previewPriceLabel)
     */
    private void updatePricePreview() {
        ReceiCategory category = categoryCombo.getValue();
        if (category == null) {
            previewPriceLabel.setText("0 VND");
            return;
        }

        try {
            double quantity = parseDoubleOrZero(quantityField.getText());
            long amount = parseLongOrZero(amountField.getText());
            long price = service.calculatePrice(category, quantity, amount);
            previewPriceLabel.setText(String.format("%,d VND", price));
        } catch (Exception e) {
            previewPriceLabel.setText("Không hợp lệ");
        }
    }

    private double parseDoubleOrZero(String text) {
        if (text == null || text.isBlank()) return 0;
        return Double.parseDouble(text.trim().replace(",", "."));
    }

    private long parseLongOrZero(String text) {
        if (text == null || text.isBlank()) return 0;
        return Long.parseLong(text.trim().replace(".", "").replace(",", ""));
    }

    /**
     * Xử lý khi người dùng bấm nút "Lưu".
     * Validate input, gọi ReceivableServices.createReceivable(), đóng dialog
     * nếu thành công.
     */
    @FXML
    private void handleSave() {
        try {
            String receiId = receiIdField.getText() == null ? "" : receiIdField.getText().trim();
            String receiName = receiNameField.getText() == null ? "" : receiNameField.getText().trim();
            ReceiCategory category = categoryCombo.getValue();
            boolean mandatory = mandatoryCheck.isSelected();
            String description = descriptionField.getText();

            double quantity = parseDoubleOrZero(quantityField.getText());
            long manualAmount = parseLongOrZero(amountField.getText());

            Receivable created = service.createReceivable(
                    receiId, receiName, category, mandatory, quantity, manualAmount, description);

            this.result = created;
            infoLabel.setStyle("-fx-text-fill: #1b8a3a;");
            infoLabel.setText("Đã tạo khoản thu thành công!");
            closeDialog();

        } catch (NumberFormatException e) {
            infoLabel.setStyle("-fx-text-fill: #e53935;");
            infoLabel.setText("Số đã sử dụng / số tiền không hợp lệ.");
        } catch (SecurityException e) {
            infoLabel.setStyle("-fx-text-fill: #e53935;");
            infoLabel.setText(e.getMessage());
        } catch (IllegalArgumentException e) {
            infoLabel.setStyle("-fx-text-fill: #e53935;");
            infoLabel.setText(e.getMessage());
        } catch (Exception e) {
            infoLabel.setStyle("-fx-text-fill: #e53935;");
            infoLabel.setText("Lỗi không xác định: " + e.getMessage());
            logger.error("Lỗi khi tạo khoản thu mới", e);
        }
    }

    /**
     * Xử lý khi người dùng bấm nút "Huỷ".
     * Đóng dialog mà không lưu gì.
     */
    @FXML
    private void handleCancel() {
        this.result = null;
        closeDialog();
    }

    /**
     * Đóng dialog hiện tại.
     */
    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Lấy kết quả sau khi dialog đóng.
     * Đầu ra: Receivable vừa tạo, hoặc null nếu người dùng huỷ / chưa lưu thành công.
     */
    public Receivable getResult() {
        return result;
    }
}
