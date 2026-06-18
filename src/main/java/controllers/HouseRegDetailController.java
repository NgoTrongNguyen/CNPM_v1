package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Apartment;
import models.HouseReg;
import models.Resident;
import models.ResidentHouse;
import repository.HouseRegRepo;
import repository.ResidentHouseRepo;
import services.HouseRegExportServices;
import user.AuthManager;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller cho dialog "Chi tiết hộ khẩu" (HouseRegDetailView.fxml).
 *
 * Hiển thị:
 *  - Thông tin hộ khẩu (mã hộ khẩu, ngày đăng ký)
 *  - Thông tin căn hộ đang sống (mã căn hộ, số phòng, diện tích, mô tả)
 *  - Bảng danh sách thành viên (mã cư dân, họ tên, ngày sinh, SĐT, vai trò)
 *  - Nút "Xuất Word" để xuất toàn bộ thông tin trên ra file .docx
 */
public class HouseRegDetailController extends BaseController {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final HouseRegRepo houseRegRepo = new HouseRegRepo();
    private final ResidentHouseRepo residentHouseRepo = new ResidentHouseRepo();
    private final HouseRegExportServices exportServices =
            new HouseRegExportServices(houseRegRepo, residentHouseRepo);

    /* ── Thông tin hộ khẩu ── */
    @FXML private Label lblHouseId;
    @FXML private Label lblCreatedAt;

    /* ── Thông tin căn hộ ── */
    @FXML private Label lblApartId;
    @FXML private Label lblRoomNumber;
    @FXML private Label lblArea;
    @FXML private Label lblDescription;

    /* ── Bảng thành viên ── */
    @FXML private TableView<ResidentHouse> memberTable;
    @FXML private TableColumn<ResidentHouse, String> colResidentId;
    @FXML private TableColumn<ResidentHouse, String> colName;
    @FXML private TableColumn<ResidentHouse, String> colBirthday;
    @FXML private TableColumn<ResidentHouse, String> colPhone;
    @FXML private TableColumn<ResidentHouse, String> colRole;

    @FXML private Button exportWordButton;
    @FXML private Button closeButton;
    @FXML private Label infoLabel;

    private HouseReg houseReg;

    @FXML
    public void initialize() {
        colResidentId.setCellValueFactory(data -> {
            Resident r = data.getValue().getResident();
            return new javafx.beans.property.SimpleStringProperty(r != null ? r.getResidentId() : "");
        });
        colName.setCellValueFactory(data -> {
            Resident r = data.getValue().getResident();
            return new javafx.beans.property.SimpleStringProperty(r != null ? r.getName() : "");
        });
        colBirthday.setCellValueFactory(data -> {
            Resident r = data.getValue().getResident();
            return new javafx.beans.property.SimpleStringProperty(
                    r != null && r.getBirthday() != null ? r.getBirthday().toString() : "");
        });
        colPhone.setCellValueFactory(data -> {
            Resident r = data.getValue().getResident();
            return new javafx.beans.property.SimpleStringProperty(
                    r != null && r.getTelephone() != null ? r.getTelephone() : "");
        });
        colRole.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().isMaster() ? "Chủ hộ" : "Thành viên"));

        if (!AuthManager.hasManagerRole()) {
            exportWordButton.setDisable(true);
            exportWordButton.setTooltip(new Tooltip("Chỉ MANAGER/ADMIN mới được xuất file."));
        }
    }

    /**
     * Nhận hộ khẩu cần hiển thị từ controller cha (HouseRegController).
     * Tải thông tin căn hộ và danh sách thành viên tương ứng.
     *
     * Đầu vào: houseReg — entity HouseReg đã chọn
     */
    public void setHouseReg(HouseReg houseReg) {
        this.houseReg = houseReg;

        // ── Thông tin hộ khẩu ──
        lblHouseId.setText(houseReg.getHouseId());
        lblCreatedAt.setText(houseReg.getCreateAt() != null
                ? DATE_FMT.format(houseReg.getCreateAt()) : "—");

        // ── Thông tin căn hộ đang sống ──
        Apartment apt = houseReg.getApartment();
        if (apt != null) {
            lblApartId.setText(apt.getApartId());
            lblRoomNumber.setText(apt.getRoomNumber());
            lblArea.setText(apt.getArea() + " m²");
            lblDescription.setText(apt.getDescription() != null ? apt.getDescription() : "—");
        } else {
            lblApartId.setText("—");
            lblRoomNumber.setText("—");
            lblArea.setText("—");
            lblDescription.setText("Chưa được gán căn hộ");
        }

        // ── Danh sách thành viên ──
        List<ResidentHouse> members = residentHouseRepo.findByHouseId(houseReg.getHouseId());
        ObservableList<ResidentHouse> data = FXCollections.observableArrayList(members);
        memberTable.setItems(data);
    }

    /**
     * Xử lý khi bấm nút "📄 Xuất Word".
     * Mở FileChooser cho người dùng chọn nơi lưu file .docx, sau đó gọi
     * HouseRegExportServices để xuất.
     */
    @FXML
    private void handleExportWord() {
        if (houseReg == null) return;

        if (!AuthManager.hasManagerRole()) {
            infoLabel.setStyle("-fx-text-fill: #e53935;");
            infoLabel.setText("Bạn không có quyền xuất file.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu thông tin hộ khẩu");
        fileChooser.setInitialFileName("ho_khau_" + houseReg.getHouseId() + ".docx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Word Document (*.docx)", "*.docx"));

        Stage stage = (Stage) closeButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return; // người dùng hủy

        try {
            exportServices.exportHouseRegDetail(houseReg.getHouseId(), file.getAbsolutePath());
            infoLabel.setStyle("-fx-text-fill: #1b8a3a;");
            infoLabel.setText("Đã xuất file thành công: " + file.getName());
        } catch (SecurityException e) {
            infoLabel.setStyle("-fx-text-fill: #e53935;");
            infoLabel.setText(e.getMessage());
        } catch (IllegalArgumentException e) {
            infoLabel.setStyle("-fx-text-fill: #e53935;");
            infoLabel.setText(e.getMessage());
        } catch (IOException e) {
            logger.error("Lỗi khi xuất file Word hộ khẩu", e);
            infoLabel.setStyle("-fx-text-fill: #e53935;");
            infoLabel.setText("Lỗi khi xuất file: " + e.getMessage());
        }
    }

    /**
     * Đóng dialog.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
