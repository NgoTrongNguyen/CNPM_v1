package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.HouseRecei;
import models.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.PaymentRepo;
import services.PaymentService;
import user.AuthManager;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Controller màn hình lịch sử thanh toán.
 * Hiển thị tất cả các lần thanh toán đã ghi nhận và tổng tiền đã thu.
 */
public class PaymentController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final PaymentService paymentService = new PaymentService(new PaymentRepo());

    /* ── Navbar ── */
    @FXML private VBox navbar;
    @FXML private NavbarController navbarController;

    /* ── Summary ── */
    @FXML private Label lblTotalCollected;

    /* ── Table ── */
    @FXML private TableView<Payment> paymentTable;
    @FXML private TableColumn<Payment, String>  colPaymentId;
    @FXML private TableColumn<Payment, Object>  colHouse;
    @FXML private TableColumn<Payment, Object>  colReceiName;
    @FXML private TableColumn<Payment, Long>    colAmount;
    @FXML private TableColumn<Payment, Instant> colDate;
    @FXML private TableColumn<Payment, String>  colMethod;
    @FXML private TableColumn<Payment, String>  colNotes;

    private final ObservableList<Payment> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (!AuthManager.hasFinanceRole()) {
            logger.warn("User không có quyền tài chính — chỉ xem lịch sử thanh toán.");
        }

        colPaymentId.setCellValueFactory(new PropertyValueFactory<>("paymentId"));

        colHouse.setCellValueFactory(new PropertyValueFactory<>("houseRecei"));
        colHouse.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    HouseRecei hr = (HouseRecei) item;
                    setText(hr.getHouseReg() != null ? hr.getHouseReg().getHouseId() : "—");
                }
            }
        });

        colReceiName.setCellValueFactory(new PropertyValueFactory<>("houseRecei"));
        colReceiName.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    HouseRecei hr = (HouseRecei) item;
                    setText(hr.getReceivable() != null ? hr.getReceivable().getReceiName() : "—");
                }
            }
        });

        colAmount.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));
        colAmount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,d VND", item));
            }
        });

        colDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Instant item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DT_FMT.format(item));
            }
        });

        colMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        paymentTable.setItems(data);
        reload();
    }

    @FXML
    private void handleReload(ActionEvent event) {
        reload();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        goBack();
    }

    private void reload() {
        data.setAll(paymentService.findAll());
        lblTotalCollected.setText(String.format("%,d VND", paymentService.getTotalCollected()));
    }
}
