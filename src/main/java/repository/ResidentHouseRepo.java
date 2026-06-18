package repository;

import database.DB_manager;
import models.Resident;
import models.ResidentHouse;
import models.HouseReg;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ResidentHouseRepo {

    /**
     * Lấy tất cả mối quan hệ cư dân - căn hộ chưa bị xóa.
     * Đầu vào: không có
     * Đầu ra: List<ResidentHouse> tất cả mối quan hệ
     */
    public List<ResidentHouse> findAll() {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM ResidentHouse WHERE resident.deleteAt IS NULL AND houseReg.deleteAt IS NULL",
                    ResidentHouse.class).list();
        }
    }

    /**
     * Lấy tất cả căn hộ của một cư dân.
     * Đầu vào: residentId — mã cư dân
     * Đầu ra: List<ResidentHouse> các căn hộ mà cư dân ở
     */
    public List<ResidentHouse> findByResidentId(String residentId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            Resident resident = session.get(Resident.class, residentId);
            if (resident == null) return List.of();
            var q = session.createQuery(
                    "FROM ResidentHouse WHERE resident = :resident", ResidentHouse.class);
            q.setParameter("resident", resident);
            return q.list();
        }
    }

    /**
     * Lấy tất cả cư dân của một căn hộ.
     * Đầu vào: houseId — mã căn hộ
     * Đầu ra: List<ResidentHouse> các cư dân ở căn hộ
     */
    public List<ResidentHouse> findByHouseId(String houseId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            HouseReg houseReg = session.get(HouseReg.class, houseId);
            if (houseReg == null) return List.of();
            var q = session.createQuery(
                    "FROM ResidentHouse WHERE houseReg = :houseReg", ResidentHouse.class);
            q.setParameter("houseReg", houseReg);
            return q.list();
        }
    }

    /**
     * Lấy chủ hộ (isMaster = true) của một căn hộ.
     * Đầu vào: houseId — mã căn hộ
     * Đầu ra: ResidentHouse (chủ hộ) hoặc null nếu không tìm thấy
     */
    public ResidentHouse findMasterByHouseId(String houseId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            HouseReg houseReg = session.get(HouseReg.class, houseId);
            if (houseReg == null) return null;
            var q = session.createQuery(
                    "FROM ResidentHouse WHERE houseReg = :houseReg AND isMaster = true",
                    ResidentHouse.class);
            q.setParameter("houseReg", houseReg);
            return q.uniqueResult();
        }
    }

    /**
     * Lấy mối quan hệ theo cư dân và căn hộ (composite key).
     * Đầu vào: residentId, houseId — khóa tổng hợp
     * Đầu ra: ResidentHouse hoặc null
     */
    public ResidentHouse findByCompositeId(String residentId, String houseId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            Resident resident = session.get(Resident.class, residentId);
            HouseReg houseReg = session.get(HouseReg.class, houseId);
            if (resident == null || houseReg == null) return null;

            var q = session.createQuery(
                    "FROM ResidentHouse WHERE resident = :resident AND houseReg = :houseReg",
                    ResidentHouse.class);
            q.setParameter("resident", resident);
            q.setParameter("houseReg", houseReg);
            return q.uniqueResult();
        }
    }

    /**
     * Thêm mối quan hệ cư dân - căn hộ mới.
     * Đầu vào: residentHouse — entity ResidentHouse
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void addResidentHouse(ResidentHouse residentHouse) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(residentHouse);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Cập nhật mối quan hệ (ví dụ: thay đổi status master).
     * Đầu vào: residentHouse — entity cần cập nhật
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void updateResidentHouse(ResidentHouse residentHouse) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(residentHouse);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Xóa mối quan hệ cư dân - căn hộ.
     * Đầu vào: residentHouse — entity cần xóa
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void deleteResidentHouse(ResidentHouse residentHouse) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            ResidentHouse managed = session.merge(residentHouse);
            session.remove(managed);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Xóa tất cả cư dân khỏi một căn hộ.
     * Đầu vào: houseId — mã căn hộ
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void deleteByHouseId(String houseId) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            HouseReg houseReg = session.get(HouseReg.class, houseId);
            if (houseReg != null) {
                List<ResidentHouse> toDelete = findByHouseId(houseId);
                for (ResidentHouse rh : toDelete) {
                    ResidentHouse managed = session.merge(rh);
                    session.remove(managed);
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}