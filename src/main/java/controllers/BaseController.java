package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import navigation.AppRouter;
import navigation.NavigationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Controller cơ sở cho tất cả JavaFX controller trong ứng dụng.
 *
 * <h3>Navigation (mới — dùng AppRouter)</h3>
 * <pre>
 *   // Điều hướng đơn giản
 *   navigate("Home");
 *
 *   // Điều hướng có truyền data
 *   navigate(NavigationEntry.of("Resident").withData("searchQuery", "Nguyen A"));
 *
 *   // Quay lại trang trước
 *   goBack();
 *
 *   // Sau login — xóa stack rồi về Home
 *   clearAndNavigate("Home");
 * </pre>
 *
 * <h3>Backward compatibility</h3>
 * {@link #switchScene(ActionEvent, String)} vẫn hoạt động nhưng bên trong
 * gọi {@link #navigate(String)} — có history stack đầy đủ.
 */
public abstract class BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    /* ════════════════════════════════════════════
       Navigation API — dùng AppRouter
    ════════════════════════════════════════════ */

    /**
     * Điều hướng đến view mới, đẩy trang hiện tại vào stack.
     *
     * @param viewName tên view (không có "View.fxml"), ví dụ: "Home", "Resident"
     */
    protected void navigate(String viewName) {
        AppRouter.getInstance().navigate(viewName);
    }

    /**
     * Điều hướng đến view mới kèm dữ liệu truyền sang.
     * Controller đích cần implement {@link navigation.Navigable} để nhận data.
     *
     * @param entry NavigationEntry chứa viewName và data
     */
    protected void navigate(NavigationEntry entry) {
        AppRouter.getInstance().navigate(entry);
    }

    /**
     * Quay lại trang trước trong stack (bao gồm trạng thái đã lưu).
     * Không làm gì nếu stack rỗng.
     */
    protected void goBack() {
        if (AppRouter.getInstance().canGoBack()) {
            AppRouter.getInstance().back();
        } else {
            logger.warn("goBack() gọi khi không còn trang trước — bị bỏ qua.");
        }
    }

    /**
     * Thay thế trang hiện tại mà KHÔNG tạo entry trong stack.
     * Dùng để refresh trang hoặc redirect không muốn tạo history.
     *
     * @param viewName tên view
     */
    protected void replaceScene(String viewName) {
        AppRouter.getInstance().replaceTop(viewName);
    }

    /**
     * Xóa toàn bộ navigation stack rồi điều hướng.
     * Dùng sau đăng nhập thành công để ngăn back về màn hình Login.
     *
     * @param viewName tên view đích
     */
    protected void clearAndNavigate(String viewName) {
        AppRouter.getInstance().clearAndNavigate(viewName);
    }

    /**
     * Xóa toàn bộ navigation stack rồi điều hướng, có kèm data.
     *
     * @param entry NavigationEntry chứa viewName và data
     */
    protected void clearAndNavigate(NavigationEntry entry) {
        AppRouter.getInstance().clearAndNavigate(entry);
    }

    /**
     * Kiểm tra có thể goBack() không.
     *
     * @return true nếu stack có ít nhất 1 entry
     */
    protected boolean canGoBack() {
        return AppRouter.getInstance().canGoBack();
    }

    /* ════════════════════════════════════════════
       Backward compatibility
       switchScene cũ vẫn chạy nhưng dùng Router
    ════════════════════════════════════════════ */

    /**
     * @deprecated Dùng {@link #navigate(String)} thay thế.
     *             Giữ lại để không phải sửa toàn bộ controller cũ ngay lập tức.
     */
    @Deprecated(since = "2.0", forRemoval = false)
    protected void switchScene(ActionEvent event, String goTo) {
        navigate(goTo);
    }

    /* ════════════════════════════════════════════
       UI Helpers
    ════════════════════════════════════════════ */

    /**
     * Hiển thị dialog xác nhận Yes/No.
     *
     * @param title   tiêu đề dialog
     * @param message nội dung hỏi
     * @return true nếu người dùng chọn "Đồng ý"
     */
    protected boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType buttonYes = new ButtonType("Đồng ý");
        ButtonType buttonNo  = new ButtonType("Hủy bỏ");
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonYes;
    }

    /**
     * Hiển thị alert thông tin.
     *
     * @param title   tiêu đề
     * @param message nội dung
     */
    protected void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * Hiển thị alert lỗi.
     *
     * @param title   tiêu đề
     * @param message nội dung lỗi
     */
    protected void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    /**
     * Hiển thị alert cảnh báo.
     *
     * @param title   tiêu đề
     * @param message nội dung
     */
    protected void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    /* ── private ── */

    private void showAlert(Alert.AlertType type, String title, String message) {
        // Phải chạy trên JavaFX thread
        if (Platform.isFxApplicationThread()) {
            buildAndShow(type, title, message);
        } else {
            Platform.runLater(() -> buildAndShow(type, title, message));
        }
    }

    private void buildAndShow(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
