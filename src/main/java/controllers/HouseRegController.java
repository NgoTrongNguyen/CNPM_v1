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
import models.Apartment;
import models.HouseReg;
import repository.HouseRegRepo;
import repository.ResidentHouseRepo;
import services.HouseRegExportServices;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller cho màn hình "Hộ khẩu" (HouseRegView.fxml).
 *
 * Hiển thị bảng danh sách house_reg (mã hộ khẩu, căn hộ, ngày đăng ký).
 * Bấm "Chi tiết" trên một dòng -> mở dialog hiển thị thông tin thành viên +
 * thông tin căn hộ đang sống, kèm nút xuất sang file Word.
 */
public class HouseRegController extends BaseController {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final HouseRegRepo houseRegRepo = new HouseRegRepo();
    private final ResidentHouseRepo residentHouseRepo = new ResidentHouseRepo();
    private final HouseRegExportServices exportServices =
            new HouseRegExportServices(houseRegRepo, residentHouseRepo);

    /* ── Navbar ── */
    @FXML private VBox navbar;
    @FXML private NavbarController navbarController;

    /* ── Table ── */
    @FXML private TableView<HouseReg> houseTable;
    @FXML private TableColumn<HouseReg, String> colHouseId;
    @FXML private TableColumn<HouseReg, String> colApartId;
    @FXML private TableColumn<HouseReg, String> colRoomNumber;
    @FXML private TableColumn<HouseReg, String> colCreatedAt;
    @FXML private TableColumn<HouseReg, Void> colAction;

    @FXML private TextField searchField;
    @FXML private Label infoLabel;

    private final ObservableList<HouseReg> houseList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colHouseId.setCellValueFactory(new PropertyValueFactory<>("houseId"));

        colApartId.setCellValueFactory(data -> {
            Apartment apt = data.getValue().getApartment();
            return new javafx.beans.property.SimpleStringProperty(apt != null ? apt.getApartId() : "");
        });

        colRoomNumber.setCellValueFactory(data -> {
            Apartment apt = data.getValue().getApartment();
            return new javafx.beans.property.SimpleStringProperty(apt != null ? apt.getRoomNumber() : "");
        });

        colCreatedAt.setCellValueFactory(data -> {
            var createAt = data.getValue().getCreateAt();
            return new javafx.beans.property.SimpleStringProperty(
                    createAt != null ? DATE_FMT.format(createAt) : "");
        });

        // ── Cột hành động: nút "Chi tiết" ──
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button detailButton = new Button("📋 Chi tiết");

            {
                detailButton.setStyle("-fx-background-color:#2196F3;-fx-text-fill:white;");
                detailButton.setOnAction(e -> {
                    HouseReg house = getTableView().getItems().get(getIndex());
                    openDetailDialog(house);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : detailButton);
            }
        });

        loadData();
    }

    /**
     * Tải lại toàn bộ danh sách hộ khẩu chưa bị xóa.
     */
    private void loadData() {
        List<HouseReg> all = houseRegRepo.findAll();
        houseList.setAll(all);
        houseTable.setItems(houseList);
    }

    /**
     * Xử lý tìm kiếm theo mã hộ khẩu hoặc mã căn hộ (lọc client-side).
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            houseTable.setItems(houseList);
            return;
        }

        ObservableList<HouseReg> filtered = houseList.filtered(h -> {
            boolean matchHouse = h.getHouseId() != null && h.getHouseId().toLowerCase().contains(keyword);
            boolean matchApt = h.getApartment() != null && h.getApartment().getApartId() != null
                    && h.getApartment().getApartId().toLowerCase().contains(keyword);
            return matchHouse || matchApt;
        });
        houseTable.setItems(filtered);
    }

    @FXML
    private void handleReload(ActionEvent event) {
        searchField.clear();
        loadData();
    }

    /**
     * Mở dialog chi tiết hộ khẩu: hiển thị thông tin thành viên + căn hộ đang
     * sống, kèm nút xuất Word.
     *
     * Đầu vào: house — entity HouseReg đã chọn
     */
    private void openDetailDialog(HouseReg house) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/HouseRegDetailView.fxml"));
            Parent root = loader.load();

            HouseRegDetailController controller = loader.getController();
            controller.setHouseReg(house);

            Stage dialog = new Stage();
            dialog.setTitle("Chi tiết hộ khẩu - " + house.getHouseId());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

        } catch (IOException e) {
            logger.error("Không thể mở dialog chi tiết hộ khẩu", e);
            showError("Lỗi", "Không thể mở chi tiết hộ khẩu: " + e.getMessage());
        }
    }

    /**
     * Hiển thị hộp thoại lỗi.
     */
    protected void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
