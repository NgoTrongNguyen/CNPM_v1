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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import models.Resident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.ResidentRepo;
import services.ResidentServices;

import java.time.LocalDate;
import java.util.List;

public class ResidentTableController {

    private static final Logger logger = LoggerFactory.getLogger(ResidentTableController.class);

    private final ResidentServices residentServices;
    private final ObservableList<Resident> masterData = FXCollections.observableArrayList();

    @FXML private TableView<Resident> myTable;
    @FXML private TableColumn<Resident, String>    idCol;
    @FXML private TableColumn<Resident, String>    nameCol;
    @FXML private TableColumn<Resident, LocalDate> birthCol;
    @FXML private TableColumn<Resident, String>    teleCol;
    @FXML private TableColumn<Resident, Void>      actionCol;

    public ResidentTableController() {
        ResidentRepo residentRepo = new ResidentRepo();
        residentServices = new ResidentServices(residentRepo);
    }

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("residentId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        birthCol.setCellValueFactory(new PropertyValueFactory<>("birthday"));
        teleCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        setupActionCol();
        reloadAll();
    }

    /* ── Nút ">" mở màn hình chi tiết ── */
    private void setupActionCol() {
        Callback<TableColumn<Resident, Void>, TableCell<Resident, Void>> cellFactory =
            param -> new TableCell<>() {
                private final Button btnDetail = new Button("Chi tiết");
                private final Button btnDelete = new Button("Xóa");

                {
                    btnDetail.setStyle(
                        "-fx-background-color: #2196F3; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-font-size: 13;");
                    btnDelete.setStyle(
                        "-fx-background-color: #e53935; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-font-size: 13;");

                    btnDetail.setOnAction((ActionEvent e) -> {
                        Resident r = getTableView().getItems().get(getIndex());
                        openDetailScreen(r);
                    });
                    btnDelete.setOnAction((ActionEvent e) -> {
                        Resident r = getTableView().getItems().get(getIndex());
                        confirmAndDelete(r);
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        javafx.scene.layout.HBox box =
                            new javafx.scene.layout.HBox(6, btnDetail, btnDelete);
                        setGraphic(box);
                    }
                }
            };
        actionCol.setCellFactory(cellFactory);
    }

    /* ── Mở dialog chi tiết / chỉnh sửa ── */
    private void openDetailScreen(Resident resident) {
        logger.info("Opening details of resident: {}", resident.getName());
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ResidentFormView.fxml"));
            Parent root = loader.load();

            ResidentFormController formController = loader.getController();
            formController.setResident(resident);   // điền sẵn dữ liệu

            Stage dialog = new Stage();
            dialog.setTitle("Chi tiết cư dân");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Resident updated = formController.getResult();
            if (updated != null) {
                // refresh row trong table
                myTable.refresh();
            }
        } catch (Exception e) {
            logger.error("Không mở được form chi tiết: {}", e.getMessage(), e);
        }
    }

    /* ── Confirm rồi xóa ── */
    private void confirmAndDelete(Resident resident) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc muốn xóa cư dân: " + resident.getName() + "?");
        ButtonType yes = new ButtonType("Đồng ý");
        ButtonType no  = new ButtonType("Hủy");
        alert.getButtonTypes().setAll(yes, no);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == yes) deleteResident(resident);
        });
    }

    /* ════════════════════════════════════════════
       Public API — dùng bởi ResidentController
    ════════════════════════════════════════════ */

    /** Tải lại toàn bộ danh sách từ DB */
    public void reloadAll() {
        List<Resident> all = residentServices.findAll();
        masterData.setAll(all != null ? all : List.of());
        myTable.setItems(masterData);
    }

    /** Tìm kiếm theo thông tin (tên / CCCD / SĐT / ngày sinh) */
    public void findResident(String info) {
        if (info == null || info.isBlank()) {
            myTable.setItems(masterData);
            return;
        }
        List<Resident> residents = residentServices.findByContainInfo(info);
        ObservableList<Resident> items =
                FXCollections.observableArrayList(residents != null ? residents : List.of());
        myTable.setItems(items);
    }

    /** Thêm cư dân mới */
    public void addResident(Resident resident) {
        if (!residentServices.addResident(resident)) {
            showErrorAlert("Không thể thêm cư dân! CCCD có thể đã tồn tại.");
        } else {
            masterData.add(resident);
        }
    }

    /** Xóa mềm cư dân */
    public void deleteResident(Resident resident) {
        if (!residentServices.deleteResident(resident)) {
            showErrorAlert("Không thể xóa cư dân!");
        } else {
            masterData.remove(resident);
        }
    }

    /* ── Helper ── */
    private void showErrorAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
