package repository;

import database.DB_manager;
import models.Receivable;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.Instant;
import java.util.List;

/**
 * Repository quản lý khoản thu định kỳ (Receivable).
 *
 * Cập nhật: Thêm method create(), update(), existsById()
 * từ INTEGRATION_SNIPPET để hỗ trợ form thêm/chỉnh sửa khoản thu.
 */
public class ReceivableRepo {

    /**
     * Lấy tất cả khoản thu định kỳ chưa bị xóa.
     * Đầu vào: không có
     * Đầu ra: List<Receivable> tất cả khoản thu
     */
    public List<Receivable> findAll() {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM Receivable WHERE deleteAt IS NULL", Receivable.class).list();
        }
    }

    /**
     * Lấy khoản thu theo mã.
     * Đầu vào: receiId — mã khoản thu
     * Đầu ra: Receivable hoặc null
     */
    public Receivable findByReceiId(String receiId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            Receivable recei = session.get(Receivable.class, receiId);
            if (recei != null && recei.getDeleteAt() != null) return null;
            return recei;
        }
    }

    /**
     * Lấy tất cả khoản thu bắt buộc (mandatory = true).
     * Đầu vào: không có
     * Đầu ra: List<Receivable> các khoản bắt buộc
     */
    public List<Receivable> findMandatory() {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM Receivable WHERE mandatory = true AND deleteAt IS NULL",
                    Receivable.class).list();
        }
    }

    /**
     * Lấy tất cả khoản thu không bắt buộc.
     * Đầu vào: không có
     * Đầu ra: List<Receivable> các khoản không bắt buộc
     */
    public List<Receivable> findOptional() {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM Receivable WHERE mandatory = false AND deleteAt IS NULL",
                    Receivable.class).list();
        }
    }

    /**
     * Lấy tất cả khoản thu có giá cố định (fixed = true).
     * Đầu vào: không có
     * Đầu ra: List<Receivable> các khoản cố định
     */
    public List<Receivable> findFixed() {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM Receivable WHERE fixed = true AND deleteAt IS NULL",
                    Receivable.class).list();
        }
    }

    /**
     * Tìm khoản thu theo tên (ILIKE pattern matching).
     * Đầu vào: name — từ khóa tìm tên khoản thu
     * Đầu ra: List<Receivable> các khoản khớp pattern
     */
    public List<Receivable> findByName(String name) {
        try (Session session = DB_manager.getFactory().openSession()) {
            var q = session.createQuery(
                    "FROM Receivable WHERE receiName ILIKE :name AND deleteAt IS NULL",
                    Receivable.class);
            q.setParameter("name", "%" + name + "%");
            return q.list();
        }
    }

    /**
     * Lấy khoản thu trong khoảng giá.
     * Đầu vào: minPrice, maxPrice — giới hạn giá
     * Đầu ra: List<Receivable> các khoản trong khoảng giá
     */
    public List<Receivable> findByPriceRange(long minPrice, long maxPrice) {
        try (Session session = DB_manager.getFactory().openSession()) {
            var q = session.createQuery(
                    "FROM Receivable WHERE price >= :minPrice AND price <= :maxPrice AND deleteAt IS NULL",
                    Receivable.class);
            q.setParameter("minPrice", minPrice);
            q.setParameter("maxPrice", maxPrice);
            return q.list();
        }
    }

    /**
     * Thêm khoản thu định kỳ mới.
     * Đầu vào: receivable — entity Receivable
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void addReceivable(Receivable receivable) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(receivable);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Cập nhật thông tin khoản thu.
     * Đầu vào: receivable — entity cần cập nhật
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void updateReceivable(Receivable receivable) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(receivable);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Xóa mềm khoản thu (set deleteAt = now).
     * Đầu vào: receivable — entity cần xóa
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void deleteReceivable(Receivable receivable) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            Receivable managed = session.merge(receivable);
            managed.setDeleteAt(Instant.now());
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Xóa mềm khoản thu theo mã.
     * Đầu vào: receiId — mã khoản thu
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void deleteByReceiId(String receiId) {
        Receivable recei = findByReceiId(receiId);
        if (recei != null) {
            deleteReceivable(recei);
        }
    }

    /**
     * Kiểm tra mã khoản thu đã tồn tại chưa (và chưa bị xóa).
     * Đầu vào: receiId — mã cần kiểm tra
     * Đầu ra: true nếu tồn tại, false nếu không
     */
    public boolean exists(String receiId) {
        return findByReceiId(receiId) != null;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Thêm từ INTEGRATION_SNIPPET — hỗ trợ form thêm khoản thu
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Tạo mới một khoản thu định kỳ (Receivable).
     * Đầu vào: receivable — entity Receivable đã set đầy đủ các field
     *          (receiId, receiName, mandatory, fixed, price, description)
     * Đầu ra: Receivable vừa được lưu (đã có createdAt do DB sinh)
     * Lưu ý: createdAt là cột insertable=false nên DB tự sinh giá trị.
     */
    public Receivable create(Receivable receivable) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(receivable);
            tx.commit();
            return receivable;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Cập nhật một khoản thu định kỳ đã tồn tại.
     * Đầu vào: receivable — entity Receivable đã có receiId và các field cần update
     * Đầu ra: không có (ném exception nếu lỗi)
     */
    public void update(Receivable receivable) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(receivable);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Kiểm tra một mã khoản thu (receiId) đã tồn tại hay chưa.
     * Đầu vào: receiId — mã khoản thu cần kiểm tra
     * Đầu ra: true nếu đã tồn tại (kể cả đã xóa mềm), false nếu chưa
     *
     * Lưu ý: Khác với exists() ở trên, method này kiểm tra cả xóa mềm.
     */
    public boolean existsById(String receiId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            Receivable r = session.get(Receivable.class, receiId);
            return r != null;
        }
    }
}