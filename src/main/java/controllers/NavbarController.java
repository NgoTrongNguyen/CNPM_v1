package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import navigation.AppRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import user.AuthManager;

/**
 * Controller sidebar Navbar.
 *
 * Dùng {@link AppRouter} để navigate thay vì tự load FXML,
 * đảm bảo navigation stack được duy trì.
 */
public class NavbarController {

    private static final Logger logger = LoggerFactory.getLogger(NavbarController.class);

    private final double MAX_WIDTH = 260;
    private final double MIN_WIDTH = 65;

    @FXML private VBox      navbar;
    @FXML private Button    homeButton;
    @FXML private Button    residentButton;
    @FXML private Button    financeButton;
    @FXML private Button    statisticsButton;
    @FXML private Button    staffManagerButton;
    @FXML private Button    houseRegButton;
    @FXML private Button    apartmentButton;
    @FXML private Button    staffDetailButton;
    @FXML private Button    logoutButton;

    public void initialize() {
        residentButton.setDisable(!AuthManager.hasResidentRole());
        financeButton.setDisable(!AuthManager.hasFinanceRole());
        staffManagerButton.setDisable(!AuthManager.hasStaffManagerRole());
        apartmentButton.setDisable(!AuthManager.hasResidentRole());

        animateSidebarMin();

        navbar.setOnMouseEntered(e -> animateSidebarMax());
        navbar.setOnMouseExited(e  -> animateSidebarMin());
    }

    /* ── Điều hướng — dùng AppRouter ── */

    @FXML
    private void homeButtonAction(ActionEvent event) {
        AppRouter.getInstance().navigate("Home");
    }

    @FXML
    private void residentButtonAction(ActionEvent event) {
        AppRouter.getInstance().navigate("Resident");
    }

    @FXML
    private void financeButtonAction(ActionEvent event) {
        AppRouter.getInstance().navigate("Finance");
    }

    @FXML
    private void statisticsButtonAction(ActionEvent event) {
        AppRouter.getInstance().navigate("Statistics");
    }

    @FXML
    private void staffManagerButtonAction(ActionEvent event) {
        AppRouter.getInstance().navigate("StaffManagement");
    }

    @FXML
    private void houseRegButtonAction(ActionEvent event) {
        AppRouter.getInstance().navigate("HouseReg");
    }

    @FXML
    private void staffDetailButtonAction(ActionEvent event) {
        AppRouter.getInstance().navigate("StaffDetails");
    }

    @FXML
    private void apartmentButtonAction(ActionEvent event) {
        AppRouter.getInstance().navigate("Apartment");
    }

    @FXML
    private void logoutButtonAction(ActionEvent event) {
        boolean confirmed = showConfirmation("Đăng xuất", "Bạn có muốn đăng xuất không?");
        if (confirmed) {
            AuthManager.logout();
            logger.info("logout successful");
            AppRouter.getInstance().clearAndNavigate("Login");
        } else {
            logger.info("User cancelled logout");
        }
    }

    /* ── Animation sidebar ── */

    private void animateSidebarMax() {
        animateTo(MAX_WIDTH);
        setButtonsFullMode(true);
    }

    private void animateSidebarMin() {
        animateTo(MIN_WIDTH);
        setButtonsFullMode(false);
    }

    private void animateTo(double width) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.millis(200),
                new KeyValue(navbar.prefWidthProperty(), width))
        );
        tl.play();
    }

    private void setButtonsFullMode(boolean full) {
        setButton(homeButton, full);
        setButton(residentButton, full);
        setButton(financeButton, full);
        setButton(statisticsButton, full);
        setButton(staffManagerButton, full);
        setButton(houseRegButton, full);
        setButton(staffDetailButton, full);
        setButton(logoutButton, full);
    }

    private void setButton(Button btn, boolean full) {
        btn.setContentDisplay(full ? ContentDisplay.LEFT : ContentDisplay.GRAPHIC_ONLY);
        btn.setPrefWidth(full ? 260 : 65);
    }

    /* ── Helper dialog ── */

    private boolean showConfirmation(String title, String message) {
        javafx.scene.control.Alert alert =
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        javafx.scene.control.ButtonType yes = new javafx.scene.control.ButtonType("Đồng ý");
        javafx.scene.control.ButtonType no  = new javafx.scene.control.ButtonType("Hủy bỏ");
        alert.getButtonTypes().setAll(yes, no);
        return alert.showAndWait()
            .filter(b -> b == yes)
            .isPresent();
    }
}
