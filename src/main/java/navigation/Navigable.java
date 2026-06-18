package navigation;

import java.util.Map;

/**
 * Interface tùy chọn cho các JavaFX controller muốn nhận dữ liệu
 * khi được {@link AppRouter} điều hướng đến.
 *
 * <p>Implement interface này trên controller để:
 * <ul>
 *   <li>Nhận data được truyền từ trang trước (navigate forward).</li>
 *   <li>Khôi phục trạng thái khi quay lại bằng back() (navigate backward).</li>
 * </ul>
 *
 * <h3>Ví dụ — controller nhận search query khi back về Resident</h3>
 * <pre>
 * public class ResidentController extends BaseController implements Navigable {
 *
 *     {@literal @}Override
 *     public void onNavigatedTo(Map{@literal <}String, Object{@literal >} data) {
 *         String query = (String) data.getOrDefault("searchQuery", "");
 *         searchField.setText(query);
 *         if (!query.isEmpty()) {
 *             residentTableController.findResident(query);
 *         } else {
 *             residentTableController.reloadAll();
 *         }
 *     }
 * }
 * </pre>
 *
 * <h3>Ví dụ — navigate và truyền data</h3>
 * <pre>
 *   // Từ ResidentController, navigate sang chi tiết với residentId
 *   AppRouter.getInstance().navigate(
 *       NavigationEntry.of("ResidentDetail")
 *           .withData("residentId", resident.getResidentId())
 *           .withData("readOnly", true)
 *   );
 * </pre>
 */
public interface Navigable {

    /**
     * Được {@link AppRouter} gọi ngay sau khi FXML load xong và scene được hiển thị.
     * Đây là nơi controller nên dùng để:
     * <ul>
     *   <li>Đọc dữ liệu được truyền tới ({@code data} map).</li>
     *   <li>Khôi phục scroll position, filter, selection...</li>
     *   <li>Kích hoạt logic phụ thuộc vào context (ví dụ: load bản ghi cụ thể).</li>
     * </ul>
     *
     * <p><b>Lưu ý:</b> Method này chạy trên JavaFX Application Thread
     * (gọi từ {@code Platform.runLater} bên trong Router).
     * Không được block thread này — nếu cần I/O hãy dùng Task hoặc CompletableFuture.
     *
     * @param data map dữ liệu được truyền từ {@link NavigationEntry}.
     *             Là unmodifiable map, không bao giờ null (có thể rỗng).
     */
    void onNavigatedTo(Map<String, Object> data);
}
