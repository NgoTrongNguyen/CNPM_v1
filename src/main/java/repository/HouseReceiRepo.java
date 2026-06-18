package repository;

import database.DB_manager;
import models.HouseRecei;
import models.HouseReg;
import models.Receivable;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.Instant;
import java.util.List;

public class HouseReceiRepo {

    /**
     * Lấy tất cả khoản thu chưa bị xóa.
     * Đầu vào: không có
     * Đầu ra: List<HouseRecei> các khoản thu
     */
    public List<HouseRecei> findAll() {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM HouseRecei WHERE id.houseReg.deleteAt IS NULL", HouseRecei.class).list();
        }
    }

    /**
     * Lấy tất cả khoản thu của một căn hộ (house_id).
     * Đầu vào: houseId — mã căn hộ
     * Đầu ra: List<HouseRecei> các khoản thu của căn hộ
     */
    public List<HouseRecei> findByHouseId(String houseId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            HouseReg houseReg = session.get(HouseReg.class, houseId);
            if (houseReg == null) return List.of();
            var q = session.createQuery(
                    "FROM HouseRecei WHERE id.houseReg = :houseReg", HouseRecei.class);
            q.setParameter("houseReg", houseReg);
            return q.list();
        }
    }

    /**
     * Lấy tất cả khoản thu theo trạng thái thanh toán.
     * Đầu vào: status — true = đã thanh toán, false = chưa thanh toán
     * Đầu ra: List<HouseRecei> các khoản có trạng thái khớp
     */
    public List<HouseRecei> findByStatus(boolean status) {
        try (Session session = DB_manager.getFactory().openSession()) {
            var q = session.createQuery(
                    "FROM HouseRecei WHERE status = :status", HouseRecei.class);
            q.setParameter("status", status);
            return q.list();
        }
    }

    /**
     * Lấy khoản thu chưa thanh toán (overdue hoặc sắp tới hạn).
     * Đầu vào: không có
     * Đầu ra: List<HouseRecei> các khoản status = false
     */
    public List<HouseRecei> findUnpaid() {
        return findByStatus(false);
    }

    /**
     * Lấy khoản thu theo houseId và receivableId.
     * Đầu vào: houseId, receiId — mã căn hộ và mã khoản thu
     * Đầu ra: HouseRecei hoặc null
     */
    public HouseRecei findByCompositeId(String houseId, String receiId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            HouseReg houseReg = session.get(HouseReg.class, houseId);
            Receivable receivable = session.get(Receivable.class, receiId);
            if (houseReg == null || receivable == null) return null;

            var q = session.createQuery(
                    "FROM HouseRecei WHERE id.houseReg = :houseReg AND id.receivable = :receivable",
                    HouseRecei.class);
            q.setParameter("houseReg", houseReg);
            q.setParameter("receivable", receivable);
            return q.uniqueResult();
        }
    }

    /**
     * Thêm khoản thu mới.
     * Đầu vào: houseRecei — entity HouseRecei
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void addHouseRecei(HouseRecei houseRecei) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(houseRecei);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Cập nhật trạng thái thanh toán của khoản thu.
     * Đầu vào: houseId, receiId — khóa tổng hợp; status — true/false
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void updateStatus(String houseId, String receiId, boolean status) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            HouseRecei houseRecei = findByCompositeId(houseId, receiId);
            if (houseRecei != null) {
                HouseRecei managed = session.merge(houseRecei);
                managed.setStatus(status);
                if (status) {
                    // Nếu đánh dấu là đã thanh toán, lưu thời gian thanh toán
                    managed.setPayDate(Instant.now());
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Cập nhật thông tin khoản thu (quantity, description, ...).
     * Đầu vào: houseRecei — entity cần cập nhật
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void updateHouseRecei(HouseRecei houseRecei) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(houseRecei);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Xóa (mềm) khoản thu.
     * Đầu vào: houseRecei — entity cần xóa
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void deleteHouseRecei(HouseRecei houseRecei) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            HouseRecei managed = session.merge(houseRecei);
            session.remove(managed);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}