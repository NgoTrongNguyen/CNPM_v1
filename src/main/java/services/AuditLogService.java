package services;

import models.ActivityLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.ActivityLogRepo;
import user.AuthManager;

import java.time.Instant;
import java.util.List;

/**
 * Service ghi nhật ký audit trail cho mọi thay đổi dữ liệu.
 *
 * <p>Cách dùng: thêm 1 dòng vào các thao tác add/update/delete của service:
 * <pre>
 *   AuditLogService.log("RESIDENT", resident.getResidentId(), "CREATE",
 *           "Created resident: " + resident.getName());
 * </pre>
 *
 * <p>Việc ghi log không bao giờ làm hỏng nghiệp vụ chính: nếu log lỗi
 * thì chỉ ghi lỗi ra logger chứ không ném exception.
 */
public class AuditLogService {

    private static final ActivityLogRepo repo = new ActivityLogRepo();
    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    /**
     * Ghi một hành động vào nhật ký.
     * Đầu vào: entityType, entityId, action (CREATE/UPDATE/DELETE), description
     * Đầu ra: không có (nuốt lỗi, chỉ log)
     */
    public static void log(String entityType, String entityId, String action, String description) {
        try {
            String staffId = AuthManager.isLoggedIn()
                    ? AuthManager.getCurrentUser().getUserId()
                    : "SYSTEM";

            ActivityLog log = new ActivityLog(entityType, entityId, action, staffId, description);
            repo.log(log);
            logger.debug("Logged: {} {} by {}", action, entityType, staffId);
        } catch (Exception e) {
            logger.error("Failed to log audit: {}", e.getMessage(), e);
        }
    }

    /**
     * Lấy lịch sử thay đổi của một entity.
     */
    public List<ActivityLog> getHistory(String entityType, String entityId) {
        try {
            return repo.findByEntity(entityType, entityId);
        } catch (Exception e) {
            logger.error("Failed to get history: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy tất cả hành động của một user.
     */
    public List<ActivityLog> getUserActions(String staffId) {
        try {
            return repo.findByStaff(staffId);
        } catch (Exception e) {
            logger.error("Failed to get user actions: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy hành động trong khoảng thời gian.
     */
    public List<ActivityLog> getActionsBetween(Instant from, Instant to) {
        try {
            return repo.findByDateRange(from, to);
        } catch (Exception e) {
            logger.error("Failed to get actions by date: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy N bản ghi nhật ký gần nhất.
     */
    public List<ActivityLog> getRecent(int limit) {
        try {
            return repo.findAll(limit);
        } catch (Exception e) {
            logger.error("Failed to get recent logs: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
