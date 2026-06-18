package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Apartment;

/**
 * Controller cho form "Thêm/Chỉnh sửa căn hộ" (ApartmentFormView.fxml).
 *
 * Dùng chung cho:
 *  - Thêm mới: gọi không tham số, sau showAndWait() lấy getResult()
 *  - Xem/sửa : gọi setApartment(apartment) trước showAndWait()
 */
public class ApartmentFormController {

    @FXML private TextField  apartIdField;
    @FXML private TextField  houseIdField;
    @FXML private TextField  roomNumberField;
    @FXML private TextField  areaField;
    @FXML private TextField  descriptionField;
    @FXML private Label      errorLabel;
    @FXML private Button     saveButton;

    private Apartment result = null;   // null = user bấm Hủy
    private Apartment _editing = null; // object đang sửa (null = thêm mới)

    /**
     * Điền dữ liệu khi xem/chỉnh sửa căn hộ đã tồn tại.
     * Đầu vào: apartment — entity Apartment cần hiển thị
     */
    public void setApartment(Apartment apartment) {
        apartIdField.setText(apartment.getApartId());
        apartIdField.setDisable(true);  // không cho sửa mã căn hộ

        houseIdField.setText(apartment.getHouseId());
        roomNumberField.setText(apartment.getRoomNumber());
        areaField.setText(String.valueOf(apartment.getArea()));
        descriptionField.setText(apartment.getDescription() != null ? apartment.getDescription() : "");

        this.result = null;
        this._editing = apartment;
    }

    /**
     * Lưu căn hộ - validate dữ liệu và tạo/cập nhật object.
     * Đầu vào: event — ActionEvent từ nút "Lưu"
     */
    @FXML
    private void handleSave(ActionEvent event) {
        errorLabel.setText("");

        String apartId = apartIdField.getText().trim();
        String houseId = houseIdField.getText().trim();
        String roomNumber = roomNumberField.getText().trim();
        String areaStr = areaField.getText().trim();
        String description = descriptionField.getText().trim();

        // Validate
        if (apartId.isEmpty()) {
            errorLabel.setText("Mã căn hộ không được để trống!");
            return;
        }
        if (houseId.isEmpty()) {
            errorLabel.setText("Mã tòa nhà không được để trống!");
            return;
        }
        if (roomNumber.isEmpty()) {
            errorLabel.setText("Số phòng không được để trống!");
            return;
        }
        if (areaStr.isEmpty()) {
            errorLabel.setText("Diện tích không được để trống!");
            return;
        }

        double area;
        try {
            area = Double.parseDouble(areaStr);
            if (area <= 0) {
                errorLabel.setText("Diện tích phải lớn hơn 0!");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Diện tích phải là số hợp lệ!");
            return;
        }

        Apartment apartment = (_editing != null) ? _editing : new Apartment();
        apartment.setApartId(apartId);
        apartment.setHouseId(houseId);
        apartment.setRoomNumber(roomNumber);
        apartment.setArea(area);
        apartment.setDescription(description.isEmpty() ? null : description);

        result = apartment;
        closeStage();
    }

    /**
     * Hủy - không lưu gì.
     * Đầu vào: event — ActionEvent từ nút "Hủy"
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        result = null;
        closeStage();
    }

    /**
     * Trả về Apartment đã nhập, hoặc null nếu người dùng bấm Hủy.
     */
    public Apartment getResult() {
        return result;
    }

    private void closeStage() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}