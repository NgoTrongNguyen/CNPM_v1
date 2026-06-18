package models;

import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;

/**
 * Bản ghi nhật ký hoạt động (audit trail).
 * Ghi lại mọi thay đổi dữ liệu trong hệ thống: ai, khi nào, làm gì.
 */
@Entity
@Table(name = "activity_log")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false)  // "RESIDENT", "STAFF", "PAYMENT", ...
    private String entityType;

    @Column(name = "entity_id", nullable = false)    // ID của entity bị tác động
    private String entityId;

    @Column(name = "action", nullable = false)       // "CREATE", "UPDATE", "DELETE"
    private String action;

    @Column(name = "staff_id", nullable = false)     // User thực hiện thay đổi
    private String staffId;

    @Generated(event = EventType.INSERT)
    @Column(name = "timestamp", insertable = false, updatable = false)
    private Instant timestamp;

    @Column(name = "description", columnDefinition = "TEXT")  // Mô tả thay đổi (old vs new)
    private String description;

    public ActivityLog() {}

    public ActivityLog(String entityType, String entityId, String action,
                       String staffId, String description) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.staffId = staffId;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public Instant getTimestamp() { return timestamp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return String.format("[%s] %s %s by %s: %s",
                timestamp, action, entityType, staffId, entityId);
    }
}
