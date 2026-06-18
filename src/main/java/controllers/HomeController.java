package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import user.AuthManager;

/**
 * Controller trang chủ.
 * Mọi navigate đi đều push vào stack — người dùng có thể back về Home.
 */
public class HomeController extends BaseController {

    @FXML private Label  staffId;
    @FXML private Button residentButton;
    @FXML private Button financeButton;
    @FXML private Button statisticsButton;
    @FXML private Button logoutButton;
    @FXML private Button staffManagerButton;
    @FXML private Button staffDetailButton;

    public void initialize() {
        staffId.setText(AuthManager.getCurrentUser().getUserId());
        staffManagerButton.setDisable(!AuthManager.hasAdminRole());
        residentButton.setDisable(!AuthManager.hasResidentRole());
        financeButton.setDisable(!AuthManager.hasFinanceRole());
    }

    @FXML private void staffDetails(ActionEvent event)    { navigate("StaffDetails"); }
    @FXML private void staffManagement(ActionEvent event) { navigate("StaffManagement"); }
    @FXML private void residentButton(ActionEvent event)  { navigate("Resident"); }
    @FXML private void financeButton(ActionEvent event)   { navigate("Finance"); }
    @FXML private void statisticsButton(ActionEvent event){ navigate("Statistics"); }

    @FXML
    private void logout(ActionEvent event) {
        try {
            if (showConfirmation("Đăng xuất", "Bạn có muốn đăng xuất không ?")) {
                AuthManager.logout();
                logger.info("logout successful");
                // Xóa stack — không back về Home sau logout
                clearAndNavigate("Login");
            } else {
                logger.info("User cancelled logout");
            }
        } catch (Exception e) {
            logger.error("logout failed: {}", e.getMessage(), e);
        }
    }
}
