package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Apartment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.ApartmentRepo;
import services.ApartmentServices;
import user.AuthManager;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller trang quản lý căn hộ (Apartment).
 *
 * Tính năng:
 *  - Hiển thị danh sách căn hộ chưa bị xóa
 *  - Tìm kiếm theo mã căn hộ hoặc số phòng
 *  - Lọc theo khoảng diện tích
 *  - Thêm mới, chỉnh sửa, xóa mềm căn hộ
 *
 * Quyền:
 *  - ADMIN/RESIDENT: xem và quản lý
 *  - Người khác: không truy cập được
 */
public class ApartmentController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ApartmentController.class);
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final ApartmentServices service = new ApartmentServices(new ApartmentRepo());
    private final ObservableList<Apartment> masterData = FXCollections.observableArrayList();

    /* ── Navbar ── */
    @FXML private VBox navbar;
    @FXML private NavbarController navbarController;

    /* ── Toolbar ── */
    @FXML private TextField searchField;
    @FXML private TextField minAreaField;
    @FXML private TextField maxAreaField;

    /* ── Table ── */
    @FXML private TableView<Apartment> apartmentTable;
    @FXML private TableColumn<Apartment, String>   colApartId;
    @FXML private TableColumn<Apartment, String>   colHouseId;
    @FXML private TableColumn<Apartment, String>   colRoomNumber;
    @FXML private TableColumn<Apartment, Double>   colArea;
    @FXML private TableColumn<Apartment, String>   colDescription;
    @FXML private TableColumn<Apartment, Instant>  colCreated;
    @FXML private TableColumn<Apartment, Void>     colAction;

    @FXML private Label infoLabel;

    @FXML
    public void initialize() {
        // Kiểm tra quyền
        if (!AuthManager.hasResidentRole()) {
            logger.warn("User {} không có quyền quản lý căn hộ",
                    AuthManager.getCurrentUser().getUserId());
            infoLabel.setText("Bạn không có quyền truy cập trang quản lý căn hộ!");
            apartmentTable.setDisable(true);
            return;
        }

        // Setup columns
        colApartId.setCellValueFactory(new PropertyValueFactory<>("apartId"));
        colHouseId.setCellValueFactory(new PropertyValueFactory<>("houseId"));
        colRoomNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colArea.setCellValueFactory(new PropertyValueFactory<>("area"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        colCreated.setCellValueFactory(new PropertyValueFactory<>("deleteAt"));
        colCreated.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Instant item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Chưa xóa" : DT_FMT.format(item));
            }
        });

        setupActionCol();
        reloadAll();
    }

    /**
     * Cấu hình cột "Hành động" với các nút: Chỉnh sửa, Xóa.
     */
    private void setupActionCol() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️ Sửa");
            private final Button btnDel = new Button("🗑️ Xóa");

            {
                btnEdit.setStyle("-fx-background-color:#2196F3;-fx-text-fill:white;-fx-font-size:11;");
                btnDel.setStyle("-fx-background-color:#e53935;-fx-text-fill:white;-fx-font-size:11;");

                btnEdit.setOnAction(e -> editApartment(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> confirmDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !AuthManager.hasAdminRole()) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(6, btnEdit, btnDel));
                }
            }
        });
    }

    /* ── Tìm kiếm ── */

    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText().trim().toLowerCase();
        String minAreaStr = minAreaField.getText().trim();
        String maxAreaStr = maxAreaField.getText().trim();

        List<Apartment> result = masterData;

        // Lọc theo keyword (mã căn hộ hoặc số phòng)
        if (!keyword.isEmpty()) {
            result = result.stream()
                    .filter(a -> a.getApartId().toLowerCase().contains(keyword)
                            || a.getRoomNumber().toLowerCase().contains(keyword))
                    .toList();
        }

        // Lọc theo diện tích
        if (!minAreaStr.isEmpty() || !maxAreaStr.isEmpty()) {
            try {
                double minArea = minAreaStr.isEmpty() ? 0 : Double.parseDouble(minAreaStr);
                double maxArea = maxAreaStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxAreaStr);
                result = result.stream()
                        .filter(a -> a.getArea() >= minArea && a.getArea() <= maxArea)
                        .toList();
            } catch (NumberFormatException e) {
                showError("Lỗi", "Vui lòng nhập số hợp lệ cho diện tích!");
                return;
            }
        }

        apartmentTable.setItems(FXCollections.observableArrayList(result));
    }

    @FXML
    private void handleReload(ActionEvent event) {
        searchField.clear();
        minAreaField.clear();
        maxAreaField.clear();
        reloadAll();
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        if (!AuthManager.hasAdminRole()) {
            showError("Không có quyền", "Chỉ ADMIN mới được thêm mới căn hộ.");
            return;
        }

        Apartment newApart = showApartmentForm(null);
        if (newApart != null) {
            if (service.addApartment(newApart)) {
                masterData.add(newApart);
                apartmentTable.setItems(masterData);
                showInfo("Thành công", "Đã thêm căn hộ mới: " + newApart.getApartId());
            } else {
                showError("Thất bại", "Không thể thêm căn hộ! Mã căn hộ có thể đã tồn tại.");
            }
        }
    }

    /* ── Internal Actions ── */

    private void editApartment(Apartment apart) {
        Apartment updated = showApartmentForm(apart);
        if (updated != null) {
            if (service.updateApartment(updated)) {
                apartmentTable.refresh();
                showInfo("Thành công", "Đã cập nhật căn hộ: " + updated.getApartId());
            } else {
                showError("Thất bại", "Không thể cập nhật căn hộ!");
            }
        }
    }

    private void confirmDelete(Apartment apart) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc muốn xóa căn hộ: " + apart.getApartId() + "?");
        ButtonType yes = new ButtonType("Đồng ý");
        ButtonType no = new ButtonType("Hủy");
        alert.getButtonTypes().setAll(yes, no);

        alert.showAndWait().ifPresent(btn -> {
            if (btn == yes) {
                if (service.deleteApartment(apart)) {
                    masterData.remove(apart);
                    apartmentTable.setItems(masterData);
                    showInfo("Thành công", "Đã xóa căn hộ: " + apart.getApartId());
                } else {
                    showError("Thất bại", "Không thể xóa căn hộ! (Có thể đang được sử dụng)");
                }
            }
        });
    }

    /**
     * Mở dialog để thêm mới hoặc chỉnh sửa căn hộ.
     *
     * @param existing null = thêm mới, Apartment = chỉnh sửa
     * @return Apartment nếu người dùng lưu, null nếu hủy
     */
    private Apartment showApartmentForm(Apartment existing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ApartmentFormView.fxml"));
            Parent root = loader.load();

            ApartmentFormController ctrl = loader.getController();
            if (existing != null) {
                ctrl.setApartment(existing);
            }

            Stage dialog = new Stage();
            dialog.setTitle(existing == null ? "Thêm căn hộ mới" : "Chỉnh sửa căn hộ");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root, 500, 400));
            dialog.showAndWait();

            return ctrl.getResult();
        } catch (Exception e) {
            logger.error("Lỗi khi mở form căn hộ: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Tải lại toàn bộ danh sách căn hộ từ DB.
     */
    private void reloadAll() {
        List<Apartment> all = service.findAll();
        masterData.setAll(all != null ? all : List.of());
        apartmentTable.setItems(masterData);
        infoLabel.setText("Tổng: " + masterData.size() + " căn hộ");
    }
}