package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import models.Resident;
import navigation.Navigable;
import navigation.NavigationEntry;

import java.util.Map;

/**
 * Controller trang Quản lý cư dân.
 *
 * <p>Implement {@link Navigable} để khôi phục trạng thái tìm kiếm khi
 * người dùng back() từ màn hình chi tiết về đây.
 *
 * <h3>Cách lưu trạng thái tìm kiếm khi navigate đi</h3>
 * Khi mở chi tiết cư dân, Controller truyền searchQuery vào entry:
 * <pre>
 *   navigate(NavigationEntry.of("ResidentDetail")
 *       .withData("residentId", resident.getResidentId())
 *       .withData("returnQuery", searchField.getText())  // lưu query để khôi phục
 *   );
 * </pre>
 * Khi back() về, {@code onNavigatedTo} sẽ nhận lại data này và set lại searchField.
 *
 * <p><b>Lưu ý hiện tại:</b> Chi tiết cư dân vẫn mở qua Dialog (modal),
 * nên chưa cần lưu state. Nếu sau này chuyển sang full-screen navigate thì
 * {@code onNavigatedTo} bên dưới sẽ tự khôi phục.
 */
public class ResidentController extends BaseController implements Navigable {

    /* ── Navbar ── */
    @FXML private VBox navbar;
    @FXML private NavbarController navbarController;

    /* ── Toolbar ── */
    @FXML private TextField searchField;

    /* ── Sub-controller của ResidentTable.fxml ── */
    @FXML private ResidentTableController residentTableController;

    /* ════════════════════════════════════════════
       Initialize
    ════════════════════════════════════════════ */

    @FXML
    public void initialize() {
        // Thêm Enter key handler cho search field
        searchField.setOnKeyPressed(this::handleSearchKeyPressed);
    }

    private void handleSearchKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleSearch(null);
            event.consume();
        }
    }

    /* ════════════════════════════════════════════
       Navigable — khôi phục trạng thái khi back()
    ════════════════════════════════════════════ */

    /**
     * Được AppRouter gọi sau khi FXML load xong.
     * Khôi phục search query và kết quả nếu được truyền vào.
     *
     * @param data map data từ NavigationEntry trước đó
     */
    @Override
    public void onNavigatedTo(Map<String, Object> data) {
        String query = (String) data.getOrDefault("searchQuery", "");
        if (!query.isEmpty()) {
            searchField.setText(query);
            residentTableController.findResident(query);
        } else {
            residentTableController.reloadAll();
        }
    }

    /* ════════════════════════════════════════════
       Tìm kiếm
    ════════════════════════════════════════════ */

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().trim();
        residentTableController.findResident(query);
    }

    /* ════════════════════════════════════════════
       Tải lại
    ════════════════════════════════════════════ */

    @FXML
    private void handleReload(ActionEvent event) {
        searchField.clear();
        residentTableController.reloadAll();
    }

    /** Mở màn hình quản lý hộ khẩu (cư dân - căn hộ). */
    @FXML
    private void handleManageHouses(ActionEvent event) {
        navigate("ResidentHouse");
    }

    /* ════════════════════════════════════════════
       Thêm cư dân mới
    ════════════════════════════════════════════ */

    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/ResidentFormView.fxml"));
            javafx.scene.Parent root = loader.load();

            ResidentFormController formController = loader.getController();

            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.setTitle("Thêm cư dân mới");
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.showAndWait();

            Resident newResident = formController.getResult();
            if (newResident != null) {
                residentTableController.addResident(newResident);
            }
        } catch (Exception e) {
            logger.error("Không mở được form thêm cư dân: {}", e.getMessage(), e);
        }
    }
}
