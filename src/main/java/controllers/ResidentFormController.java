package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Resident;

/**
 * Dùng chung cho:
 *  - Thêm mới: gọi không tham số, sau showAndWait() lấy getResult()
 *  - Xem/sửa : gọi setResident(resident) trước showAndWait()
 */
public class ResidentFormController {

    @FXML private TextField  idField;
    @FXML private TextField  nameField;
    @FXML private DatePicker birthdayPicker;
    @FXML private TextField  teleField;
    @FXML private Label      errorLabel;
    @FXML private Button     saveButton;

    private Resident result = null;   // null = user bấm Hủy

    /* ────────────────────────────────────────────
       Điền dữ liệu khi xem chi tiết
    ──────────────────────────────────────────── */
    public void setResident(Resident resident) {
        idField.setText(resident.getResidentId());
        idField.setDisable(true);                   // không cho sửa CCCD
        nameField.setText(resident.getName());
        if (resident.getBirthday() != null) {
            birthdayPicker.setValue(resident.getBirthday());
        }
        teleField.setText(resident.getTelephone() != null ? resident.getTelephone() : "");
        // Giữ reference để update
        this.result = null;
        this._editing = resident;
    }

    private Resident _editing = null;

    /* ────────────────────────────────────────────
       Lưu
    ──────────────────────────────────────────── */
    @FXML
    private void handleSave(ActionEvent event) {
        errorLabel.setText("");

        String id   = idField.getText().trim();
        String name = nameField.getText().trim();

        // Validate bắt buộc
        if (id.isEmpty()) {
            errorLabel.setText("CCCD / Mã cư dân không được để trống!");
            return;
        }
        if (name.isEmpty()) {
            errorLabel.setText("Họ và tên không được để trống!");
            return;
        }

        Resident resident = (_editing != null) ? _editing : new Resident();
        resident.setResidentId(id);
        resident.setName(name);
        resident.setBirthday(birthdayPicker.getValue());
        String tele = teleField.getText().trim();
        resident.setTelephone(tele.isEmpty() ? null : tele);

        result = resident;
        closeStage();
    }

    /* ────────────────────────────────────────────
       Hủy
    ──────────────────────────────────────────── */
    @FXML
    private void handleCancel(ActionEvent event) {
        result = null;
        closeStage();
    }

    /** Trả về Resident đã nhập, hoặc null nếu người dùng bấm Hủy */
    public Resident getResult() {
        return result;
    }

    private void closeStage() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
