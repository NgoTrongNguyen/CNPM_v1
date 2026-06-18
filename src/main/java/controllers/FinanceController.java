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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.HouseRecei;
import models.HouseReg;
import models.Receivable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.HouseReceiRepo;
import repository.ReceivableRepo;
import services.HouseReceiServices;
import services.ReceivableServices;
import user.AuthManager;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller trang quản lý khoản thu (Finance).
 * Hiển thị tất cả khoản thu, cho phép tìm kiếm, lọc theo trạng thái thanh toán.
 *
 * Cập nhật: Thêm nút "➕ Thêm khoản thu" chỉ hiện với MANAGER/ADMIN
 */
public class FinanceController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(FinanceController.class);
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final ReceivableServices receivableServices =
            new ReceivableServices(new ReceivableRepo());
    private final HouseReceiServices houseReceiServices =
            new HouseReceiServices(new HouseReceiRepo());

    private final ObservableList<HouseRecei> masterData = FXCollections.observableArrayList();

    /* ── Navbar ── */
    @FXML private VBox navbar;
    @FXML private NavbarController navbarController;

    /* ── Toolbar ── */
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    @FXML private Button btnAddReceivable;  // ← Nút "Thêm khoản thu"

    /* ── Table ── */
    @FXML private TableView<HouseRecei>            houseReceiTable;
    @FXML private TableColumn<HouseRecei, HouseReg>   colHouseId;
    @FXML private TableColumn<HouseRecei, Receivable> colReceiName;
    @FXML private TableColumn<HouseRecei, Long>       colQuantity;
    @FXML private TableColumn<HouseRecei, HouseReg>   colPrice;   // uses row item, type matches PropertyValueFactory("houseReg")
    @FXML private TableColumn<HouseRecei, Instant>    colDeadline;
    @FXML private TableColumn<HouseRecei, Boolean>    colStatus;
    @FXML private TableColumn<HouseRecei, Void>       colAction;

    @FXML
    public void initialize() {
        if (!AuthManager.hasFinanceRole()) {
            logger.warn("User {} không có quyền truy cập Finance",
                    AuthManager.getCurrentUser().getUserId());
        }

        // ─────────────────────────────────────────────────────────────────
        // Ẩn nút "Thêm khoản thu" nếu không là MANAGER/ADMIN
        // ─────────────────────────────────────────────────────────────────
        if (!AuthManager.hasManagerRole()) {
            btnAddReceivable.setVisible(false);
            btnAddReceivable.setManaged(false);
        }

        // Status filter
        statusFilter.getItems().addAll("Tất cả", "Chưa thanh toán", "Đã thanh toán");
        statusFilter.getSelectionModel().selectFirst();

        // ── cột House ID ────────────────────────────────────────────────
        colHouseId.setCellValueFactory(new PropertyValueFactory<>("houseReg"));
        colHouseId.setCellFactory(col -> new TableCell<HouseRecei, HouseReg>() {
            @Override
            protected void updateItem(HouseReg item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getHouseId());
            }
        });

        // ── cột Tên khoản thu ───────────────────────────────────────────
        colReceiName.setCellValueFactory(new PropertyValueFactory<>("receivable"));
        colReceiName.setCellFactory(col -> new TableCell<HouseRecei, Receivable>() {
            @Override
            protected void updateItem(Receivable item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getReceiName());
            }
        });

        // ── cột Số lượng ────────────────────────────────────────────────
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // ── cột Thành tiền (price × quantity) ──────────────────────────
        // Dùng PropertyValueFactory("houseReg") chỉ để trigger refresh;
        // giá trị thực lấy từ getTableRow().getItem() bên trong updateItem.
        colPrice.setCellValueFactory(new PropertyValueFactory<>("houseReg"));
        colPrice.setCellFactory(col -> new TableCell<HouseRecei, HouseReg>() {
            @Override
            protected void updateItem(HouseReg item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    return;
                }
                HouseRecei hr = getTableRow().getItem();
                if (hr != null && hr.getReceivable() != null) {
                    long total = hr.getReceivable().getPrice() * hr.getQuantity();
                    setText(String.format("%,d VND", total));
                } else {
                    setText(null);
                }
            }
        });

        // ── cột Hạn nộp ─────────────────────────────────────────────────
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("payDeadline"));
        colDeadline.setCellFactory(col -> new TableCell<HouseRecei, Instant>() {
            @Override
            protected void updateItem(Instant item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DT_FMT.format(item));
            }
        });

        // ── cột Trạng thái ──────────────────────────────────────────────
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<HouseRecei, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "✓ Đã thanh toán" : "✗ Chưa thanh toán");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });

        setupActionCol();
        reloadAll();
    }

    private void setupActionCol() {
        colAction.setCellFactory(param -> new TableCell<HouseRecei, Void>() {
            private final Button btnMark = new Button("Đánh dấu thanh toán");

            {
                btnMark.setStyle("-fx-background-color:#4CAF50;-fx-text-fill:white;-fx-font-size:12;");
                btnMark.setOnAction(e -> {
                    HouseRecei hr = getTableView().getItems().get(getIndex());
                    if (!hr.isStatus()) {
                        markAsPaid(hr);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HouseRecei hr = getTableView().getItems().get(getIndex());
                    btnMark.setDisable(hr.isStatus());
                    setGraphic(btnMark);
                }
            }
        });
    }

    /**
     * Mở form "Thêm khoản thu mới". Chỉ MANAGER/ADMIN mới thấy nút này.
     * Sau khi lưu thành công, refresh lại danh sách/biểu đồ khoản thu.
     *
     * ─────────────────────────────────────────────────────────────────
     * Tích hợp từ INTEGRATION_SNIPPET.java — handleAddReceivable()
     * ─────────────────────────────────────────────────────────────────
     */
    @FXML
    private void handleAddReceivable() {
        if (!AuthManager.hasManagerRole()) {
            showError("Không có quyền", "Bạn không có quyền thêm khoản thu mới.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ReceivableFormView.fxml"));
            Parent root = loader.load();

            ReceivableFormController formController = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Thêm khoản thu mới");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Receivable created = formController.getResult();
            if (created != null) {
                // Refresh lại dữ liệu hiển thị (danh sách khoản thu)
                reloadAll();
                showInfo("Thành công", "Khoản thu mới đã được thêm thành công!");
            }
        } catch (IOException e) {
            logger.error("Không thể mở form thêm khoản thu", e);
            showError("Lỗi", "Không thể mở form thêm khoản thu: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().trim().toLowerCase();
        String status = statusFilter.getValue();

        List<HouseRecei> result = masterData.stream()
                .filter(hr -> query.isEmpty() ||
                        (hr.getHouseReg() != null && hr.getHouseReg().getHouseId().toLowerCase().contains(query)) ||
                        (hr.getReceivable() != null && hr.getReceivable().getReceiName().toLowerCase().contains(query)))
                .toList();

        if (status != null && !status.equals("Tất cả")) {
            boolean paid = status.equals("Đã thanh toán");
            result = result.stream().filter(hr -> hr.isStatus() == paid).toList();
        }

        houseReceiTable.setItems(FXCollections.observableArrayList(result));
    }

    @FXML
    private void handleReload(ActionEvent event) {
        searchField.clear();
        statusFilter.getSelectionModel().selectFirst();
        reloadAll();
    }

    /** Mở màn hình lịch sử thanh toán. */
    @FXML
    private void handleViewPayments(ActionEvent event) {
        navigate("Payment");
    }

    @FXML
    private void handleExportExcel(ActionEvent event) {
        if (!AuthManager.hasManagerRole()) {
            showError("Không có quyền", "Bạn không có quyền xuất báo cáo.");
            return;
        }

        try {
            String filePath = System.getProperty("user.home") + "/khoanthu_export_"
                    + System.currentTimeMillis() + ".xlsx";
            services.ExportServices exportServices = new services.ExportServices(
                    new repository.HouseReceiRepo(),
                    new repository.PaymentRepo(),
                    new repository.ResidentRepo(),
                    new repository.ResidentHouseRepo()
            );
            exportServices.exportResidentList(filePath);
            showInfo("Thành công", "Xuất báo cáo thành công: " + filePath);
        } catch (Exception e) {
            logger.error("Xuất Excel thất bại", e);
            showError("Lỗi", "Xuất Excel thất bại: " + e.getMessage());
        }
    }

    private void markAsPaid(HouseRecei houseRecei) {
        if (houseReceiServices.updateStatus(
                houseRecei.getHouseReg().getHouseId(),
                houseRecei.getReceivable().getReceiId(),
                true)) {
            houseReceiTable.refresh();
            showInfo("Thành công", "Đã đánh dấu khoản thu là thanh toán");
        } else {
            showError("Lỗi", "Không thể cập nhật trạng thái thanh toán");
        }
    }

    private void reloadAll() {
        masterData.setAll(houseReceiServices.findAll());
        houseReceiTable.setItems(masterData);
    }
}