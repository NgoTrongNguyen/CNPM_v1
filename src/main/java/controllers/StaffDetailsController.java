package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import models.StaffDetail;
import repository.StaffDetailRepo;
import repository.StaffRepo;
import services.StaffDetailServices;
import user.AuthManager;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class StaffDetailsController extends BaseController {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final StaffDetailServices service =
            new StaffDetailServices(new StaffDetailRepo(), new StaffRepo());

    /* ── Navbar ── */
    @FXML private VBox navbar;
    @FXML private NavbarController navbarController;

    /* ── Hiển thị ── */
    @FXML private Label lblStaffId;
    @FXML private Label lblRole;
    @FXML private Label lblCreatedAt;

    /* ── Chỉnh sửa thông tin ── */
    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private Label     infoLabel;

    /* ── Đổi mật khẩu ── */
    @FXML private PasswordField oldPassField;
    @FXML private PasswordField newPassField;
    @FXML private PasswordField confirmPassField;
    @FXML private Label         passLabel;

    private StaffDetail currentDetail;

    @FXML
    public void initialize() {
        // Header từ AuthManager (không cần DB)
        lblStaffId.setText(AuthManager.getCurrentUser().getUserId());
        lblRole.setText(AuthManager.getCurrentUser().getRole());

        // Load StaffDetail từ DB
        currentDetail = service.getMyDetail();
        if (currentDetail != null) {
            nameField.setText(currentDetail.getName() != null ? currentDetail.getName() : "");
            addressField.setText(currentDetail.getAddress() != null ? currentDetail.getAddress() : "");
            if (currentDetail.getCreatedAt() != null) {
                lblCreatedAt.setText(DT_FMT.format(currentDetail.getCreatedAt()));
            }
        }
    }

    /* ── Lưu thông tin cá nhân ── */
    @FXML
    private void handleSaveInfo(ActionEvent event) {
        infoLabel.setText("");
        if (currentDetail == null) {
            infoLabel.setStyle("-fx-text-fill:#e53935;");
            infoLabel.setText("Không tìm thấy thông tin nhân viên!");
            return;
        }
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            infoLabel.setStyle("-fx-text-fill:#e53935;");
            infoLabel.setText("Họ tên không được để trống!");
            return;
        }
        currentDetail.setName(name);
        String addr = addressField.getText().trim();
        currentDetail.setAddress(addr.isEmpty() ? null : addr);

        if (service.updateDetail(currentDetail)) {
            infoLabel.setStyle("-fx-text-fill:#2196F3;");
            infoLabel.setText("Cập nhật thành công!");
        } else {
            infoLabel.setStyle("-fx-text-fill:#e53935;");
            infoLabel.setText("Cập nhật thất bại!");
        }
    }

    /* ── Quay lại ── */
    @FXML
    private void handleBack(ActionEvent event) {
        goBack();
    }

    /* ── Đổi mật khẩu ── */
    @FXML
    private void handleChangePassword(ActionEvent event) {
        passLabel.setText("");
        String oldPass  = oldPassField.getText();
        String newPass  = newPassField.getText();
        String confirm  = confirmPassField.getText();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            passLabel.setText("Vui lòng điền đầy đủ các trường mật khẩu!");
            return;
        }
        if (!newPass.equals(confirm)) {
            passLabel.setText("Mật khẩu mới và xác nhận không khớp!");
            return;
        }
        if (newPass.length() < 6) {
            passLabel.setText("Mật khẩu mới phải có ít nhất 6 ký tự!");
            return;
        }

        if (service.changePassword(oldPass, newPass)) {
            passLabel.setStyle("-fx-text-fill:#2196F3;");
            passLabel.setText("Đổi mật khẩu thành công!");
            oldPassField.clear();
            newPassField.clear();
            confirmPassField.clear();
        } else {
            passLabel.setStyle("-fx-text-fill:#e53935;");
            passLabel.setText("Mật khẩu hiện tại không đúng!");
        }
    }
}
