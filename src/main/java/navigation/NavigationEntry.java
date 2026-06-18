package navigation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Đại diện cho một trang trong navigation stack.
 * Lưu tên FXML và dữ liệu trạng thái tùy chọn để khôi phục khi back().
 *
 * <p>Ví dụ:
 * <pre>
 *   NavigationEntry entry = NavigationEntry.of("Resident")
 *       .withData("searchQuery", "Nguyen Van A")
 *       .withData("scrollPosition", 42);
 * </pre>
 */
public class NavigationEntry {

    private final String viewName;
    private final Map<String, Object> data;

    private NavigationEntry(String viewName, Map<String, Object> data) {
        this.viewName = viewName;
        this.data     = Collections.unmodifiableMap(new HashMap<>(data));
    }

    /* ── Factory ── */

    /** Tạo entry không có data */
    public static NavigationEntry of(String viewName) {
        return new NavigationEntry(viewName, Map.of());
    }

    /** Tạo entry với một cặp key-value */
    public static NavigationEntry of(String viewName, String key, Object value) {
        return new NavigationEntry(viewName, Map.of(key, value));
    }

    /** Tạo entry với map data cho sẵn */
    public static NavigationEntry of(String viewName, Map<String, Object> data) {
        return new NavigationEntry(viewName, data);
    }

    /* ── Builder-style fluent API ── */

    /**
     * Thêm / ghi đè một giá trị data, trả về instance MỚI (immutable-style).
     *
     * @param key   khóa dữ liệu
     * @param value giá trị dữ liệu
     * @return NavigationEntry mới với data được cập nhật
     */
    public NavigationEntry withData(String key, Object value) {
        Map<String, Object> merged = new HashMap<>(this.data);
        merged.put(key, value);
        return new NavigationEntry(this.viewName, merged);
    }

    /* ── Getters ── */

    /** Tên view, tương ứng với file {viewName}View.fxml */
    public String getViewName() {
        return viewName;
    }

    /** Toàn bộ data map (unmodifiable) */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Lấy một giá trị từ data map, cast sang kiểu mong muốn.
     *
     * @param key  khóa cần lấy
     * @param type class của kiểu mong muốn
     * @param <T>  kiểu trả về
     * @return giá trị đã cast, hoặc null nếu không tồn tại
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object val = data.get(key);
        if (val == null) return null;
        if (type.isInstance(val)) return (T) val;
        throw new ClassCastException(
            "Key '" + key + "': expected " + type.getSimpleName()
            + " but got " + val.getClass().getSimpleName());
    }

    /** Lấy giá trị kiểu String */
    public String getString(String key) {
        return get(key, String.class);
    }

    /** Lấy giá trị kiểu Integer */
    public Integer getInt(String key) {
        return get(key, Integer.class);
    }

    /** Kiểm tra có data với key không */
    public boolean hasData(String key) {
        return data.containsKey(key);
    }

    @Override
    public String toString() {
        return "NavigationEntry{view='" + viewName + "', data=" + data + "}";
    }
}
