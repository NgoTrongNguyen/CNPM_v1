# Router Migration Guide

## Kiến trúc tổng quan

```
navigation/
├── AppRouter.java        ← Singleton, quản lý navigation stack
├── NavigationEntry.java  ← Snapshot 1 trang (viewName + data)
└── Navigable.java        ← Interface để nhận data khi được navigate tới

controllers/
└── BaseController.java   ← Cung cấp navigate(), goBack(), clearAndNavigate()...
```

---

## Thay đổi bắt buộc

### 1. Main.java — init Router
```java
@Override
public void start(Stage stage) throws Exception {
    AppRouter.getInstance().init(stage);   // ← thêm dòng này
    // ... load LoginView như cũ
}
```

### 2. Controller cũ dùng switchScene → không cần sửa
`switchScene(event, "Home")` vẫn hoạt động — bên trong nó gọi `navigate()`.
Nhưng nên đổi dần sang API mới cho rõ ràng.

---

## API mới trong BaseController

| Method | Mô tả |
|--------|-------|
| `navigate("Home")` | Đi tới Home, push trang hiện tại vào stack |
| `navigate(entry)` | Đi tới view kèm data |
| `goBack()` | Quay lại trang trước (có data) |
| `replaceScene("X")` | Thay trang hiện tại, KHÔNG tạo history |
| `clearAndNavigate("X")` | Xóa stack rồi navigate (dùng sau login/logout) |
| `canGoBack()` | Kiểm tra có thể back không |

---

## Truyền data giữa các màn hình

### Bước 1 — Navigate kèm data (controller nguồn)
```java
// Ví dụ: từ ResidentController mở chi tiết một cư dân
navigate(
    NavigationEntry.of("ResidentDetail")
        .withData("residentId", resident.getResidentId())
        .withData("returnQuery", searchField.getText())  // lưu để khôi phục
);
```

### Bước 2 — Nhận data (controller đích implement Navigable)
```java
public class ResidentDetailController extends BaseController implements Navigable {

    @Override
    public void onNavigatedTo(Map<String, Object> data) {
        String id = (String) data.get("residentId");
        loadResident(id);
    }
}
```

### Bước 3 — Back kèm data trả về (nếu cần)
```java
// Back và truyền data về trang trước
AppRouter.getInstance().back();
// Trang trước đã có "returnQuery" trong NavigationEntry của nó
// → onNavigatedTo sẽ được gọi với data đó tự động
```

---

## Ví dụ migrate từng controller

### StaffManagementController
```java
// Cũ:
switchScene(event, "Home");

// Mới:
navigate("Home");
```

### StaffDetailsController — thêm nút Back
```java
// Trong FXML thêm:
// <Button text="← Quay lại" onAction="#handleBack" />

@FXML
private void handleBack(ActionEvent event) {
    goBack();
}
```

### NavbarController — đã được cập nhật
Dùng `AppRouter.getInstance().navigate(...)` trực tiếp vì Navbar
không extend BaseController.

---

## Khôi phục trạng thái tìm kiếm (pattern phổ biến)

```java
// Màn hình danh sách (ví dụ ResidentController)
public class ResidentController extends BaseController implements Navigable {

    @FXML private TextField searchField;
    @FXML private ResidentTableController residentTableController;

    @Override
    public void onNavigatedTo(Map<String, Object> data) {
        // Được gọi cả khi navigate tới lần đầu VÀ khi back() về
        String query = (String) data.getOrDefault("searchQuery", "");
        searchField.setText(query);
        if (!query.isEmpty()) {
            residentTableController.findResident(query);
        } else {
            residentTableController.reloadAll();
        }
    }

    // Khi mở chi tiết, lưu search query để back() về khôi phục
    private void openDetail(Resident r) {
        navigate(
            NavigationEntry.of("ResidentDetail")
                .withData("residentId",  r.getResidentId())
                .withData("searchQuery", searchField.getText())
        );
    }
}
```

---

## Lưu ý quan trọng

1. **Stack size** mặc định tối đa 20 entry — tránh memory leak khi navigate nhiều.
2. **clearAndNavigate** sau login/logout — đảm bảo không back về Login.
3. **replaceScene** khi refresh trang — không tạo duplicate trong stack.
4. **Dialog (modal)** vẫn dùng Stage/showAndWait như cũ — Router chỉ quản lý full-screen navigation.
5. **NavbarController** không extend BaseController nên gọi AppRouter trực tiếp.
