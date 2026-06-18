package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import models.HouseRecei;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.HouseReceiRepo;
import repository.ResidentRepo;
import services.HouseReceiServices;
import services.ResidentServices;
import user.AuthManager;

import java.util.List;

/**
 * Controller trang Thống kê.
 * Hiển thị các thống kê cơ bản: số cư dân, khoản thu chưa thanh toán, v.v.
 */
public class StatisticsController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    private final ResidentServices residentServices =
            new ResidentServices(new ResidentRepo());
    private final HouseReceiServices houseReceiServices =
            new HouseReceiServices(new HouseReceiRepo());

    /* ── Navbar ── */
    @FXML private VBox navbar;
    @FXML private NavbarController navbarController;

    /* ── Statistics labels ── */
    @FXML private Label lblTotalResidents;
    @FXML private Label lblUnpaidCount;
    @FXML private Label lblUnpaidTotal;
    @FXML private Label lblPaidTotal;

    /* ── Charts ── */
    @FXML private BarChart<String, Number> paymentChart;

    @FXML
    public void initialize() {
        if (!AuthManager.hasManagerRole()) {
            logger.warn("User {} không có quyền xem thống kê",
                    AuthManager.getCurrentUser().getUserId());
        }

        loadStatistics();
    }

    /** Mở màn hình nhật ký hoạt động. */
    @FXML
    private void handleViewActivityLog() {
        navigate("ActivityLog");
    }

    private void loadStatistics() {
        // Tổng cư dân
        long totalResidents = residentServices.findAll().size();
        lblTotalResidents.setText(String.valueOf(totalResidents));

        // Khoản thu chưa thanh toán
        List<HouseRecei> unpaidList = houseReceiServices.findUnpaid();
        lblUnpaidCount.setText(String.valueOf(unpaidList.size()));

        long unpaidTotal = 0;
        long paidTotal = 0;

        for (HouseRecei hr : unpaidList) {
            if (hr.getReceivable() != null) {
                unpaidTotal += hr.getReceivable().getPrice() * hr.getQuantity();
            }
        }

        List<HouseRecei> allList = houseReceiServices.findAll();
        for (HouseRecei hr : allList) {
            if (hr.isStatus() && hr.getReceivable() != null) {
                paidTotal += hr.getReceivable().getPrice() * hr.getQuantity();
            }
        }

        lblUnpaidTotal.setText(String.format("%,d VND", unpaidTotal));
        lblPaidTotal.setText(String.format("%,d VND", paidTotal));

        // Chart
        loadPaymentChart(unpaidTotal, paidTotal);
    }

    private void loadPaymentChart(long unpaid, long paid) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Khoản thu");

        series.getData().add(new XYChart.Data<>("Chưa thanh toán", unpaid));
        series.getData().add(new XYChart.Data<>("Đã thanh toán", paid));

        ObservableList<XYChart.Series<String, Number>> data = FXCollections.observableArrayList(series);
        paymentChart.setData(data);
    }
}