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
import models.Staff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.StaffManagerRepo;
import services.StaffManagerServices;
import user.AuthManager;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class StaffManagementController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(StaffManagementController.class);
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final StaffManagerServices service =
            new StaffManagerServices(new StaffManagerRepo());
    private final ObservableList<Staff> masterData = FXCollections.observableArrayList();

    /** Map staffId → tên nhân viên, dùng để hiển thị cột "Họ và tên" */
    private Map<String, String> staffNameMap = Map.of();

    /* ── Navbar ── */
    @FXML private VBox navbar;
    @FXML private NavbarController navbarController;

    /* ── Toolbar ── */
    @FXML private TextField  searchField;
    @FXML private ComboBox<String> roleFilter;

    /* ── Table ── */
    @FXML private TableView<Staff>          staffTable;
    @FXML private TableColumn<Staff, String>  colId;
    @FXML private TableColumn<Staff, String>  colName;
    @FXML private TableColumn<Staff, String>  colRole;
    @FXML private TableColumn<Staff, Instant> colCreated;
    @FXML private TableColumn<Staff, Void>    colAction;

    /**
     * Khởi tạo controller: setup role filter theo quyền hạn, cấu hình các cột bảng,
     * và load dữ liệu ban đầu.
     * Đầu vào: không có (gọi tự động bởi JavaFX)
     * Đầu ra: không có
     */
    @FXML
    public void initialize() {
        // Role filter — chỉ hiện các role mà user có quyền quản lý
        List<String> manageableRoles = AuthManager.getManageableRoles();
        roleFilter.getItems().add("Tất cả");
        roleFilter.getItems().addAll(manageableRoles);
        roleFilter.getSelectionModel().selectFirst();

        // Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // colName — hiển thị tên từ StaffDetail thay vì staffId
        colName.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        colName.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String staffId, boolean empty) {
                super.updateItem(staffId, empty);
                if (empty || staffId == null) {
                    setText(null);
                } else {
                    setText(staffNameMap.getOrDefault(staffId, "—"));
                }
            }
        });

        // createdAt -> format
        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colCreated.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Instant item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DT_FMT.format(item));
            }
        });

        setupActionCol();
        reloadAll();
    }

    /**
     * Cấu hình cột "Thao tác" với các nút: Đổi role, Đổi MK, Xóa.
     * Nút "Đổi role" chỉ hiện cho ADMIN.
     * Đầu vào: không có
     * Đầu ra: không có
     */
    private void setupActionCol() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnRole = new Button("Đổi role");
            private final Button btnPass = new Button("Đổi MK");
            private final Button btnDel  = new Button("Xóa");

            {
                btnRole.setStyle("-fx-background-color:#FF9800;-fx-text-fill:white;-fx-font-size:12;");
                btnPass.setStyle("-fx-background-color:#2196F3;-fx-text-fill:white;-fx-font-size:12;");
                btnDel.setStyle("-fx-background-color:#e53935;-fx-text-fill:white;-fx-font-size:12;");

                btnRole.setOnAction(e -> changeRole(getTableView().getItems().get(getIndex())));
                btnPass.setOnAction(e -> changePassword(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e  -> confirmDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Chỉ ADMIN mới thấy nút "Đổi role"
                    if (AuthManager.hasAdminRole()) {
                        setGraphic(new HBox(6, btnRole, btnPass, btnDel));
                    } else {
                        setGraphic(new HBox(6, btnPass, btnDel));
                    }
                }
            }
        });
    }

    /* ── Handlers ── */

    /**
     * Xử lý sự kiện tìm kiếm: lọc theo keyword và/hoặc role filter.
     * Đầu vào: event — ActionEvent từ nút "Tìm kiếm"
     * Đầu ra: không có (cập nhật bảng trực tiếp)
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String q    = searchField.getText().trim();
        String role = roleFilter.getValue();
        List<Staff> result;

        if (role != null && !role.equals("Tất cả")) {
            // Lọc theo role cụ thể
            if (q.isEmpty()) {
                result = service.findByRole(role);
            } else {
                // Tìm kiếm trong role cụ thể
                result = service.search(q).stream()
                        .filter(s -> s.getRole().equals(role))
                        .toList();
            }
        } else {
            // Tìm kiếm trong phạm vi quyền hạn
            if (q.isEmpty()) {
                result = service.findAll();
            } else {
                result = service.search(q);
            }
        }

        updateStaffNameMap(result);
        staffTable.setItems(FXCollections.observableArrayList(result));
    }

    /**
     * Xử lý sự kiện tải lại: xóa keyword, reset role filter, load lại toàn bộ.
     * Đầu vào: event — ActionEvent từ nút "Tải lại"
     * Đầu ra: không có (cập nhật bảng trực tiếp)
     */
    @FXML
    private void handleReload(ActionEvent event) {
        searchField.clear();
        roleFilter.getSelectionModel().selectFirst();
        reloadAll();
    }

    /**
     * Xử lý sự kiện thêm nhân viên mới: mở form nhập liệu.
     * Đầu vào: event — ActionEvent từ nút "Thêm mới"
     * Đầu ra: không có (mở dialog và cập nhật bảng nếu thêm thành công)
     */
    @FXML
    private void handleAdd(ActionEvent event) {
        showStaffForm(null);
    }

    /* ── Internal actions ── */

    /**
     * Mở dialog đổi role cho nhân viên (chỉ ADMIN).
     * Đầu vào: staff — nhân viên cần đổi role
     * Đầu ra: không có (cập nhật bảng nếu đổi thành công)
     */
    private void changeRole(Staff staff) {
        List<String> allRoles = List.of("ADMIN", "MANAGER", "RESIDENT", "FINANCE");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(staff.getRole(), allRoles);
        dialog.setTitle("Đổi vai trò");
        dialog.setHeaderText("Nhân viên: " + staff.getStaffId());
        dialog.setContentText("Chọn vai trò mới:");
        dialog.showAndWait().ifPresent(newRole -> {
            if (!newRole.equals(staff.getRole())) {
                if (service.updateRole(staff, newRole)) {
                    reloadAll();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Không thể đổi vai trò!");
                }
            }
        });
    }

    /**
     * Mở dialog đổi mật khẩu cho nhân viên.
     * Đầu vào: staff — nhân viên cần đổi mật khẩu
     * Đầu ra: không có
     */
    private void changePassword(Staff staff) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("Nhân viên: " + staff.getStaffId());
        dialog.setContentText("Mật khẩu mới:");
        dialog.showAndWait().ifPresent(newPass -> {
            if (!newPass.isBlank()) {
                if (!service.updatePassword(staff, newPass)) {
                    showAlert(Alert.AlertType.ERROR, "Không thể đổi mật khẩu!");
                }
            }
        });
    }

    /**
     * Hiện dialog xác nhận xóa mềm nhân viên.
     * Đầu vào: staff — nhân viên cần xóa
     * Đầu ra: không có (cập nhật bảng nếu xóa thành công)
     */
    private void confirmDelete(Staff staff) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Xóa nhân viên " + staff.getStaffId() + "?");
        ButtonType yes = new ButtonType("Đồng ý");
        ButtonType no  = new ButtonType("Hủy");
        alert.getButtonTypes().setAll(yes, no);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == yes) {
                if (service.deleteStaff(staff)) {
                    masterData.remove(staff);
                    updateStaffNameMap(masterData);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Không thể xóa! (Có thể đang xóa chính mình hoặc không có quyền)");
                }
            }
        });
    }

    /**
     * Mở form thêm nhân viên mới hoặc xem chi tiết.
     * Sau khi thêm thành công, giả lập gửi email và hiện thông báo.
     * Đầu vào: existing — null nếu thêm mới, Staff nếu xem chi tiết
     * Đầu ra: không có (cập nhật bảng nếu thêm thành công)
     */
    private void showStaffForm(Staff existing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/StaffFormView.fxml"));
            Parent root = loader.load();
            StaffFormController ctrl = loader.getController();
            if (existing != null) ctrl.setStaff(existing);

            Stage dialog = new Stage();
            dialog.setTitle(existing == null ? "Thêm nhân viên" : "Chi tiết nhân viên");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            StaffFormController.Result r = ctrl.getResult();
            if (r != null) {
                if (service.addStaff(r.staff(), r.detail())) {
                    masterData.add(r.staff());
                    updateStaffNameMap(masterData);

                    // Giả lập gửi email thông tin tài khoản
                    String email = r.email();
                    if (email != null && !email.isBlank()) {
                        service.sendAccountEmail(email, r.staff());
                        showAlert(Alert.AlertType.INFORMATION,
                                "Đã gửi thông tin tài khoản qua email cho nhân viên!\n" +
                                "Email: " + email + "\n" +
                                "Mã NV: " + r.staff().getStaffId());
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Không thể thêm! Mã nhân viên đã tồn tại hoặc không có quyền.");
                }
            }
        } catch (Exception e) {
            logger.error("Không mở được StaffFormView: {}", e.getMessage(), e);
        }
    }

    /**
     * Tải lại toàn bộ danh sách staff theo quyền hạn và cập nhật bảng.
     * Đầu vào: không có
     * Đầu ra: không có (cập nhật masterData và staffTable)
     */
    private void reloadAll() {
        masterData.setAll(service.findAll());
        updateStaffNameMap(masterData);
        staffTable.setItems(masterData);
    }

    /**
     * Cập nhật map staffId → tên nhân viên từ StaffDetail.
     * Đầu vào: staffList — danh sách Staff hiện đang hiển thị
     * Đầu ra: không có (cập nhật staffNameMap nội bộ)
     */
    private void updateStaffNameMap(List<Staff> staffList) {
        staffNameMap = service.getStaffNameMap(staffList);
    }

    /**
     * Hiển thị alert dialog.
     * Đầu vào: type — loại alert; msg — nội dung thông báo
     * Đầu ra: không có
     */
    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
