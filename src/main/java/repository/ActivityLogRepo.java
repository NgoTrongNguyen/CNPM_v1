package repository;

import database.DB_manager;
import models.ActivityLog;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.Instant;
import java.util.List;

/**
 * Repository truy cập bảng activity_log (nhật ký hoạt động).
 */
public class ActivityLogRepo {

    /**
     * Lưu một bản ghi nhật ký.
     * Đầu vào: log — entity ActivityLog
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void log(ActivityLog log) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(log);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Lấy lịch sử thay đổi của một entity cụ thể.
     */
    public List<ActivityLog> findByEntity(String entityType, String entityId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM ActivityLog WHERE entityType = :type AND entityId = :id ORDER BY timestamp DESC",
                    ActivityLog.class)
                    .setParameter("type", entityType)
                    .setParameter("id", entityId)
                    .list();
        }
    }

    /**
     * Lấy tất cả hành động của một nhân viên.
     */
    public List<ActivityLog> findByStaff(String staffId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM ActivityLog WHERE staffId = :staff ORDER BY timestamp DESC",
                    ActivityLog.class)
                    .setParameter("staff", staffId)
                    .list();
        }
    }

    /**
     * Lấy hành động trong khoảng thời gian.
     */
    public List<ActivityLog> findByDateRange(Instant from, Instant to) {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM ActivityLog WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC",
                    ActivityLog.class)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .list();
        }
    }

    /**
     * Lọc theo loại hành động (CREATE/UPDATE/DELETE).
     */
    public List<ActivityLog> findByAction(String action) {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM ActivityLog WHERE action = :action ORDER BY timestamp DESC",
                    ActivityLog.class)
                    .setParameter("action", action)
                    .list();
        }
    }

    /**
     * Lấy N bản ghi gần nhất.
     */
    public List<ActivityLog> findAll(int limit) {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM ActivityLog ORDER BY timestamp DESC",
                    ActivityLog.class)
                    .setMaxResults(limit)
                    .list();
        }
    }
}
