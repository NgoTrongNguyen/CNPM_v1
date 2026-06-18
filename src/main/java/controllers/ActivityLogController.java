package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.ActivityLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.AuditLogService;
import user.AuthManager;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller màn hình nhật ký hoạt động (audit trail).
 * Hiển thị các thay đổi dữ liệu gần đây, lọc theo loại hành động.
 */
public class ActivityLogController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogController.class);
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final int RECENT_LIMIT = 500;

    private final AuditLogService auditLogService = new AuditLogService();

    /* ── Navbar ── */
    @FXML private VBox navbar;
    @FXML private NavbarController navbarController;

    /* ── Toolbar ── */
    @FXML private ComboBox<String> actionFilter;

    /* ── Table ── */
    @FXML private TableView<ActivityLog> logTable;
    @FXML private TableColumn<ActivityLog, Instant> colTime;
    @FXML private TableColumn<ActivityLog, String>  colAction;
    @FXML private TableColumn<ActivityLog, String>  colEntityType;
    @FXML private TableColumn<ActivityLog, String>  colEntityId;
    @FXML private TableColumn<ActivityLog, String>  colStaff;
    @FXML private TableColumn<ActivityLog, String>  colDescription;

    private final ObservableList<ActivityLog> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (!AuthManager.hasManagerRole()) {
            logger.warn("User không có quyền quản lý — chỉ xem nhật ký.");
        }

        actionFilter.getItems().addAll("Tất cả", "CREATE", "UPDATE", "DELETE");
        actionFilter.getSelectionModel().selectFirst();

        colTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colTime.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Instant item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DT_FMT.format(item));
            }
        });

        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colEntityType.setCellValueFactory(new PropertyValueFactory<>("entityType"));
        colEntityId.setCellValueFactory(new PropertyValueFactory<>("entityId"));
        colStaff.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        logTable.setItems(data);
        reload();
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        reload();
    }

    @FXML
    private void handleReload(ActionEvent event) {
        actionFilter.getSelectionModel().selectFirst();
        reload();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        goBack();
    }

    private void reload() {
        List<ActivityLog> all = auditLogService.getRecent(RECENT_LIMIT);
        String filter = actionFilter.getValue();
        if (filter != null && !filter.equals("Tất cả")) {
            all = all.stream().filter(l -> filter.equals(l.getAction())).toList();
        }
        data.setAll(all);
    }
}
