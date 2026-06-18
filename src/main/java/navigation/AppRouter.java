package navigation;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Singleton Router điều hướng toàn ứng dụng JavaFX.
 *
 * <h3>Tính năng</h3>
 * <ul>
 *   <li>Navigation stack — hỗ trợ {@link #back()} về đúng trạng thái trước.</li>
 *   <li>Truyền data giữa các view qua {@link NavigationEntry}.</li>
 *   <li>{@link #clearAndNavigate} — reset stack (dùng khi Login → Home).</li>
 *   <li>{@link #replaceTop} — thay thế trang hiện tại, không tích stack.</li>
 *   <li>Tự động gọi {@link Navigable#onNavigatedTo} trên controller đích.</li>
 * </ul>
 *
 * <h3>Khởi tạo — gọi 1 lần trong {@code Main.start()}</h3>
 * <pre>
 *   AppRouter.getInstance().init(primaryStage);
 * </pre>
 *
 * <h3>Điều hướng</h3>
 * <pre>
 *   // Đơn giản
 *   AppRouter.getInstance().navigate("Home");
 *
 *   // Có data
 *   AppRouter.getInstance().navigate(
 *       NavigationEntry.of("Resident").withData("searchQuery", "Nguyen A")
 *   );
 *
 *   // Quay lại
 *   AppRouter.getInstance().back();
 * </pre>
 */
public class AppRouter {

    private static final Logger logger = LoggerFactory.getLogger(AppRouter.class);

    /* Giới hạn độ sâu stack, tránh memory leak khi navigate nhiều */
    private static final int MAX_STACK_SIZE = 20;

    /* ── Singleton ── */
    private static final AppRouter INSTANCE = new AppRouter();

    public static AppRouter getInstance() {
        return INSTANCE;
    }

    private AppRouter() {}

    /* ── State ── */
    private Stage primaryStage;

    /**
     * Stack lưu lịch sử điều hướng.
     * Phần tử đỉnh (peek/peekFirst) = trang hiện tại.
     * Dùng ArrayDeque như stack: push = addFirst, pop = removeFirst.
     */
    private final Deque<NavigationEntry> stack = new ArrayDeque<>();

    /* ════════════════════════════════════════════
       Khởi tạo
    ════════════════════════════════════════════ */

    /**
     * Gán Stage chính. Phải gọi trước bất kỳ navigate nào.
     *
     * @param stage primaryStage của Application
     */
    public void init(Stage stage) {
        this.primaryStage = stage;
        logger.info("AppRouter initialized with primary stage.");
    }

    /**
     * Lấy Stage hiện tại từ một Node bất kỳ (dùng trong controller khi
     * chưa có reference tới primaryStage).
     *
     * @param anyNode node thuộc scene hiện tại
     * @return Stage của node đó
     */
    public Stage stageFrom(Node anyNode) {
        return (Stage) anyNode.getScene().getWindow();
    }

    /* ════════════════════════════════════════════
       Navigate — push vào stack
    ════════════════════════════════════════════ */

    /**
     * Điều hướng đến view mới, đẩy entry hiện tại vào stack.
     *
     * @param viewName tên view (không có "View.fxml")
     */
    public void navigate(String viewName) {
        navigate(NavigationEntry.of(viewName));
    }

    /**
     * Điều hướng đến view mới, đẩy entry hiện tại vào stack.
     * Controller đích nhận {@link NavigationEntry#getData()} qua {@link Navigable#onNavigatedTo}.
     *
     * @param target entry chứa viewName và data tùy chọn
     */
    public void navigate(NavigationEntry target) {
        Platform.runLater(() -> {
            if (primaryStage == null) {
                logger.error("AppRouter chưa được init! Gọi AppRouter.getInstance().init(stage) trong Main.start()");
                return;
            }
            pushCurrent();           // lưu trạng thái trang hiện tại
            loadAndShow(target);     // load trang mới
        });
    }

    /* ════════════════════════════════════════════
       Replace — thay thế top, không tích stack
    ════════════════════════════════════════════ */

    /**
     * Thay thế trang hiện tại mà KHÔNG đẩy vào stack.
     * Dùng khi muốn refresh trang hoặc redirect mà không tạo entry back.
     *
     * @param viewName tên view
     */
    public void replaceTop(String viewName) {
        replaceTop(NavigationEntry.of(viewName));
    }

    /**
     * Thay thế trang hiện tại mà KHÔNG đẩy vào stack.
     *
     * @param target entry mới
     */
    public void replaceTop(NavigationEntry target) {
        Platform.runLater(() -> {
            if (primaryStage == null) {
                logger.error("AppRouter chưa được init!");
                return;
            }
            // Không push, chỉ load trang mới
            loadAndShow(target);
        });
    }

    /* ════════════════════════════════════════════
       ClearAndNavigate — reset stack (Login → Home)
    ════════════════════════════════════════════ */

    /**
     * Xóa toàn bộ stack rồi điều hướng đến view mới.
     * Dùng sau Login (không muốn back về trang Login nữa).
     *
     * @param viewName tên view
     */
    public void clearAndNavigate(String viewName) {
        clearAndNavigate(NavigationEntry.of(viewName));
    }

    /**
     * Xóa toàn bộ stack rồi điều hướng đến view mới.
     *
     * @param target entry mới
     */
    public void clearAndNavigate(NavigationEntry target) {
        Platform.runLater(() -> {
            if (primaryStage == null) {
                logger.error("AppRouter chưa được init!");
                return;
            }
            stack.clear();
            loadAndShow(target);
        });
    }

    /* ════════════════════════════════════════════
       Back — pop stack
    ════════════════════════════════════════════ */

    /**
     * Quay lại trang trước trong stack, cùng dữ liệu trạng thái lúc rời đi.
     * Nếu stack rỗng, không làm gì (log cảnh báo).
     */
    public void back() {
        Platform.runLater(() -> {
            if (stack.isEmpty()) {
                logger.warn("back() gọi khi stack rỗng — không có trang nào để quay lại.");
                return;
            }
            NavigationEntry previous = stack.removeFirst();
            loadAndShow(previous);
            logger.info("back() → {}", previous.getViewName());
        });
    }

    /**
     * Quay lại N bước.
     *
     * @param steps số bước muốn quay lại (≥ 1)
     */
    public void back(int steps) {
        if (steps <= 0) return;
        for (int i = 0; i < steps - 1; i++) {
            if (!stack.isEmpty()) stack.removeFirst(); // bỏ qua các trang trung gian
        }
        back();
    }

    /**
     * Quay lại trang đầu tiên trong stack (root).
     * Hữu ích khi muốn về Home từ bất kỳ đâu.
     */
    public void backToRoot() {
        Platform.runLater(() -> {
            if (stack.isEmpty()) {
                logger.warn("backToRoot() gọi khi stack rỗng.");
                return;
            }
            NavigationEntry root = null;
            while (!stack.isEmpty()) {
                root = stack.removeFirst();
            }
            loadAndShow(root);
            logger.info("backToRoot() → {}", root.getViewName());
        });
    }

    /* ════════════════════════════════════════════
       Truy vấn trạng thái
    ════════════════════════════════════════════ */

    /** @return true nếu có thể back (stack không rỗng) */
    public boolean canGoBack() {
        return !stack.isEmpty();
    }

    /** @return tên view hiện tại đang hiển thị, hoặc empty nếu chưa navigate lần nào */
    public Optional<String> currentView() {
        // currentView không nằm trong stack mà đang hiển thị trên stage
        // Không track ở đây để tránh phức tạp; dùng stack.peek() = trang *trước*
        return stack.isEmpty() ? Optional.empty()
                               : Optional.of(stack.peekFirst().getViewName());
    }

    /** @return số lượng entry đang có trong stack (không tính trang đang hiện) */
    public int stackSize() {
        return stack.size();
    }

    /* ════════════════════════════════════════════
       Internal helpers
    ════════════════════════════════════════════ */

    /**
     * Snapshot trang hiện tại vào stack trước khi navigate đi.
     * Stack giữ tối đa MAX_STACK_SIZE entry — xóa entry cũ nhất nếu tràn.
     */
    private void pushCurrent() {
        // Không push nếu chưa có gì trên stage (lần navigate đầu tiên)
        if (primaryStage.getScene() == null) return;

        // Lấy viewName từ userData của scene (được set trong loadAndShow)
        Object tag = primaryStage.getScene().getUserData();
        if (tag instanceof NavigationEntry entry) {
            if (stack.size() >= MAX_STACK_SIZE) {
                stack.removeLast(); // bỏ entry cũ nhất (FIFO bên đáy)
            }
            stack.addFirst(entry);
            logger.debug("push → stack[{}]: {}", stack.size(), entry.getViewName());
        }
    }

    /**
     * Load FXML, gán Scene lên Stage, và gọi {@link Navigable#onNavigatedTo} nếu controller implement.
     *
     * @param entry entry cần hiển thị
     */
    private void loadAndShow(NavigationEntry entry) {
        try {
            String fxmlPath = "/view/" + entry.getViewName() + "View.fxml";
            URL resource = AppRouter.class.getResource(fxmlPath);

            // Thêm null-check để báo lỗi rõ ràng
            if (resource == null) {
                logger.error("Không tìm thấy FXML: {} — file chưa được tạo hoặc đường dẫn sai",
                        fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            // Gắn entry vào scene userData để pushCurrent() đọc lại sau
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
            } else {
                scene.setRoot(root);
            }
            scene.setUserData(entry);
            primaryStage.setScene(scene);
            primaryStage.show();

            // Notify controller nếu implement Navigable
            Object controller = loader.getController();
            if (controller instanceof Navigable navigable) {
                navigable.onNavigatedTo(entry.getData());
                logger.debug("onNavigatedTo called on {} with data={}",
                    entry.getViewName(), entry.getData());
            }

            logger.info("navigate → {} | stackSize={}", entry.getViewName(), stack.size());

        } catch (IOException e) {
            logger.error("Không load được FXML '{}View.fxml': {}", entry.getViewName(), e.getMessage(), e);
        }
    }
}
