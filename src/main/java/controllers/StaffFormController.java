package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Staff;
import models.StaffDetail;
import user.AuthManager;

import java.time.LocalDate;
import java.util.List;

public class StaffFormController {

    /**
     * Kết quả trả về khi người dùng bấm Lưu.
     * Chứa Staff, StaffDetail, và email để gửi thông tin tài khoản.
     */
    public record Result(Staff staff, StaffDetail detail, String email) {}

    @FXML private TextField     idField;
    @FXML private PasswordField passField;
    @FXML private ComboBox<String> roleBox;
    @FXML private TextField     nameField;
    @FXML private DatePicker    birthdayPicker;
    @FXML private TextField     addressField;
    @FXML private TextField     emailField;
    @FXML private Label         errorLabel;
    @FXML private Button        saveButton;

    private Result result = null;

    /**
     * Khởi tạo form: load danh sách role theo quyền hạn user hiện tại.
     * - ADMIN: hiện tất cả role
     * - RESIDENT/FINANCE: chỉ hiện role tương ứng
     * Đầu vào: không có (gọi tự động bởi JavaFX)
     * Đầu ra: không có
     */
    @FXML
    public void initialize() {
        List<String> manageableRoles = AuthManager.getManageableRoles();
        roleBox.getItems().addAll(manageableRoles);
        // Chọn mặc định role đầu tiên trong danh sách
        if (!manageableRoles.isEmpty()) {
            if (manageableRoles.contains("RESIDENT")) {
                roleBox.getSelectionModel().select("RESIDENT");
            } else {
                roleBox.getSelectionModel().selectFirst();
            }
        }
    }

    /**
     * Điền sẵn dữ liệu khi xem/sửa nhân viên đã tồn tại.
     * Ẩn trường mật khẩu và email khi ở chế độ xem.
     * Đầu vào: staff — entity Staff cần hiển thị
     * Đầu ra: không có
     */
    public void setStaff(Staff staff) {
        idField.setText(staff.getStaffId());
        idField.setDisable(true);
        roleBox.setValue(staff.getRole());
        // password ẩn đi khi view mode
        passField.setDisable(true);
        passField.setPromptText("Dùng chức năng 'Đổi mật khẩu'");
        // email ẩn đi khi view mode
        emailField.setDisable(true);
        emailField.setPromptText("Chỉ dùng khi thêm mới");
    }

    /**
     * Xử lý sự kiện "Lưu": validate dữ liệu và tạo Result.
     * Kiểm tra: mã NV, mật khẩu, role, họ tên không được trống; email phải hợp lệ.
     * Đầu vào: event — ActionEvent từ nút "Lưu"
     * Đầu ra: không có (kết quả lưu trong biến result)
     */
    @FXML
    private void handleSave(ActionEvent event) {
        errorLabel.setText("");

        String id   = idField.getText().trim();
        String pass = passField.getText();
        String role = roleBox.getValue();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (id.isEmpty())   { errorLabel.setText("Mã nhân viên không được trống!"); return; }
        if (pass.isEmpty() && !idField.isDisabled()) {
            errorLabel.setText("Mật khẩu không được trống!"); return;
        }
        if (role == null)   { errorLabel.setText("Vui lòng chọn vai trò!"); return; }
        if (name.isEmpty()) { errorLabel.setText("Họ tên không được trống!"); return; }
        if (!idField.isDisabled() && (email.isEmpty() || !email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))) {
            errorLabel.setText("Email không hợp lệ!"); return;
        }

        Staff staff = new Staff();
        staff.setStaffId(id);
        staff.setPassword(pass);
        staff.setRole(role);

        StaffDetail detail = new StaffDetail();
        detail.setName(name);
        detail.setBirthday(birthdayPicker.getValue() != null
                ? birthdayPicker.getValue() : LocalDate.now());
        String addr = addressField.getText().trim();
        detail.setAddress(addr.isEmpty() ? null : addr);

        result = new Result(staff, detail, email);
        closeStage();
    }

    /**
     * Xử lý sự kiện "Hủy": đóng form mà không lưu.
     * Đầu vào: event — ActionEvent từ nút "Hủy"
     * Đầu ra: không có
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        result = null;
        closeStage();
    }

    /**
     * Lấy kết quả sau khi form đóng.
     * Đầu vào: không có
     * Đầu ra: Result chứa Staff, StaffDetail, email; hoặc null nếu hủy
     */
    public Result getResult() { return result; }

    /**
     * Đóng cửa sổ form.
     * Đầu vào: không có
     * Đầu ra: không có
     */
    private void closeStage() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }
}
