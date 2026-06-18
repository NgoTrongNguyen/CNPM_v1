package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.HouseReg;
import models.Resident;
import models.ResidentHouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.HouseRegRepo;
import repository.ResidentHouseRepo;
import repository.ResidentRepo;
import services.HouseRegServices;
import services.ResidentHouseServices;
import services.ResidentServices;
import user.AuthManager;

import java.util.List;

/**
 * Controller quản lý mối quan hệ cư dân-căn hộ (hộ khẩu).
 *
 * <p>Hai panel song song:
 * <ul>
 *   <li>Trái: chọn căn hộ → xem/xóa/chỉ định chủ hộ cho cư dân trong căn hộ.</li>
 *   <li>Phải: chọn cư dân → xem các căn hộ mà cư dân thuộc về.</li>
 * </ul>
 */
public class ResidentHouseController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ResidentHouseController.class);

    private final ResidentHouseServices residentHouseServices =
            new ResidentHouseServices(new ResidentHouseRepo());
    private final ResidentServices residentServices =
            new ResidentServices(new ResidentRepo());
    private final HouseRegServices houseRegServices =
            new HouseRegServices(new HouseRegRepo());

    /* ── Navbar ── */
    @FXML private VBox navbar;
    @FXML private NavbarController navbarController;

    /* ── Selection dropdowns ── */
    @FXML private ComboBox<String> houseIdCombo;
    @FXML private ComboBox<String> residentIdCombo;

    /* ── Residents-in-house panel ── */
    @FXML private Label selectedHouseLabel;
    @FXML private TableView<ResidentHouse> residentsInHouseTable;
    @FXML private TableColumn<ResidentHouse, Object> colResidentId;
    @FXML private TableColumn<ResidentHouse, Object> colResidentName;
    @FXML private TableColumn<ResidentHouse, Boolean> colIsMaster;
    @FXML private TableColumn<ResidentHouse, Void> colAction;

    /* ── Houses-of-resident panel ── */
    @FXML private Label selectedResidentLabel;
    @FXML private TableView<ResidentHouse> housesOfResidentTable;
    @FXML private TableColumn<ResidentHouse, Object> colHouseId;
    @FXML private TableColumn<ResidentHouse, Boolean> colResidIsMaster;

    /* ── Buttons ── */
    @FXML private Button addButton;
    @FXML private Label statusLabel;

    private final ObservableList<ResidentHouse> residentsInHouse = FXCollections.observableArrayList();
    private final ObservableList<ResidentHouse> housesOfResident = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        boolean canManage = AuthManager.hasManagerRole();
        if (!canManage) {
            logger.warn("User không có quyền quản lý hộ khẩu — chỉ xem.");
            addButton.setDisable(true);
        }

        loadHouseCombo();
        loadResidentCombo();

        setupResidentsInHouseTable();
        setupHousesOfResidentTable();

        houseIdCombo.setOnAction(e -> onHouseSelected());
        residentIdCombo.setOnAction(e -> onResidentSelected());
    }

    /* ════════════════════════════════════════════
       Load combo box
    ════════════════════════════════════════════ */

    private void loadHouseCombo() {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (HouseReg h : houseRegServices.findAll()) {
            items.add(h.getHouseId());
        }
        houseIdCombo.setItems(items);
    }

    private void loadResidentCombo() {
        ObservableList<String> items = FXCollections.observableArrayList();
        List<Resident> residents = residentServices.findAll();
        if (residents != null) {
            for (Resident r : residents) {
                items.add(r.getResidentId() + " - " + r.getName());
            }
        }
        residentIdCombo.setItems(items);
    }

    /* ════════════════════════════════════════════
       Table setup
    ════════════════════════════════════════════ */

    private void setupResidentsInHouseTable() {
        colResidentId.setCellValueFactory(new PropertyValueFactory<>("resident"));
        colResidentId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : ((Resident) item).getResidentId());
            }
        });

        colResidentName.setCellValueFactory(new PropertyValueFactory<>("resident"));
        colResidentName.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : ((Resident) item).getName());
            }
        });

        colIsMaster.setCellValueFactory(new PropertyValueFactory<>("master"));
        colIsMaster.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Chủ hộ" : "Thành viên"));
            }
        });

        setupActionColForResidentsInHouse();
        residentsInHouseTable.setItems(residentsInHouse);
    }

    private void setupActionColForResidentsInHouse() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnRemove = new Button("Xóa");
            private final Button btnSetMaster = new Button("Chủ hộ");

            {
                btnRemove.setStyle("-fx-background-color:#e53935;-fx-text-fill:white;-fx-font-size:12;");
                btnSetMaster.setStyle("-fx-background-color:#2196F3;-fx-text-fill:white;-fx-font-size:12;");

                btnRemove.setOnAction(e -> removeResident(getTableView().getItems().get(getIndex())));
                btnSetMaster.setOnAction(e -> setAsMaster(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    boolean canManage = AuthManager.hasManagerRole();
                    btnRemove.setDisable(!canManage);
                    btnSetMaster.setDisable(!canManage);
                    setGraphic(new HBox(6, btnSetMaster, btnRemove));
                }
            }
        });
    }

    private void setupHousesOfResidentTable() {
        colHouseId.setCellValueFactory(new PropertyValueFactory<>("houseReg"));
        colHouseId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : ((HouseReg) item).getHouseId());
            }
        });

        colResidIsMaster.setCellValueFactory(new PropertyValueFactory<>("master"));
        colResidIsMaster.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Chủ hộ" : "Thành viên"));
            }
        });

        housesOfResidentTable.setItems(housesOfResident);
    }

    /* ════════════════════════════════════════════
       Selection handlers
    ════════════════════════════════════════════ */

    @FXML
    private void onHouseSelected() {
        String houseId = houseIdCombo.getValue();
        if (houseId == null) return;
        selectedHouseLabel.setText("Căn hộ: " + houseId);
        residentsInHouse.setAll(residentHouseServices.findResidentsInHouse(houseId));
    }

    @FXML
    private void onResidentSelected() {
        String selection = residentIdCombo.getValue();
        if (selection == null) return;
        String residentId = selection.split(" - ")[0];
        selectedResidentLabel.setText("Cư dân: " + residentId);
        housesOfResident.setAll(residentHouseServices.findHousesOfResident(residentId));
    }

    /* ════════════════════════════════════════════
       Actions
    ════════════════════════════════════════════ */

    @FXML
    private void handleAddResident() {
        String houseId = houseIdCombo.getValue();
        String residentSelection = residentIdCombo.getValue();

        if (houseId == null || residentSelection == null) {
            showError("Lỗi", "Vui lòng chọn cả căn hộ và cư dân.");
            return;
        }

        String residentId = residentSelection.split(" - ")[0];

        HouseReg house = houseRegServices.findByHouseId(houseId);
        Resident resident = findResidentById(residentId);

        if (house == null || resident == null) {
            showError("Lỗi", "Không tìm thấy căn hộ hoặc cư dân.");
            return;
        }

        if (residentHouseServices.addResidentToHouse(resident, house, false)) {
            statusLabel.setText("✓ Đã thêm cư dân vào căn hộ.");
            onHouseSelected();
        } else {
            showError("Lỗi", "Không thể thêm cư dân (đã tồn tại hoặc không đủ quyền).");
        }
    }

    @FXML
    private void handleBack() {
        goBack();
    }

    private void removeResident(ResidentHouse rh) {
        if (showConfirmation("Xóa cư dân",
                "Xóa " + rh.getResident().getResidentId() +
                        " khỏi căn hộ " + rh.getHouseReg().getHouseId() + "?")) {
            if (residentHouseServices.removeResidentFromHouse(
                    rh.getResident().getResidentId(),
                    rh.getHouseReg().getHouseId())) {
                statusLabel.setText("✓ Đã xóa khỏi căn hộ.");
                onHouseSelected();
            } else {
                showError("Lỗi", "Không thể xóa.");
            }
        }
    }

    private void setAsMaster(ResidentHouse rh) {
        boolean newStatus = !rh.isMaster();
        if (residentHouseServices.setMasterOfHouse(
                rh.getResident().getResidentId(),
                rh.getHouseReg().getHouseId(),
                newStatus)) {
            statusLabel.setText("✓ Đã cập nhật chủ hộ.");
            onHouseSelected();
        } else {
            showError("Lỗi", "Không thể cập nhật chủ hộ.");
        }
    }

    private Resident findResidentById(String residentId) {
        List<Resident> all = residentServices.findAll();
        if (all == null) return null;
        return all.stream()
                .filter(r -> r.getResidentId().equals(residentId))
                .findFirst()
                .orElse(null);
    }
}
