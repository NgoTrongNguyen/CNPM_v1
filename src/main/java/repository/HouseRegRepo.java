package repository;

import database.DB_manager;
import models.HouseReg;
import models.Apartment;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.Instant;
import java.util.List;

public class HouseRegRepo {

    /**
     * Lấy tất cả đăng ký căn hộ chưa bị xóa.
     * Đầu vào: không có
     * Đầu ra: List<HouseReg> tất cả đăng ký
     */
    public List<HouseReg> findAll() {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM HouseReg WHERE deleteAt IS NULL", HouseReg.class).list();
        }
    }

    /**
     * Lấy đăng ký theo mã house.
     * Đầu vào: houseId — mã đăng ký
     * Đầu ra: HouseReg hoặc null
     */
    public HouseReg findByHouseId(String houseId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            HouseReg house = session.get(HouseReg.class, houseId);
            if (house != null && house.getDeleteAt() != null) return null;
            return house;
        }
    }

    /**
     * Lấy tất cả đăng ký của một căn hộ (apartment).
     * Đầu vào: apartId — mã căn hộ
     * Đầu ra: List<HouseReg> các đăng ký cho căn hộ
     */
    public List<HouseReg> findByApartmentId(String apartId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            Apartment apt = session.get(Apartment.class, apartId);
            if (apt == null) return List.of();

            var q = session.createQuery(
                    "FROM HouseReg WHERE apartment = :apt AND deleteAt IS NULL", HouseReg.class);
            q.setParameter("apt", apt);
            return q.list();
        }
    }

    /**
     * Lấy tất cả đăng ký theo apartment (tìm kiếm pattern matching).
     * Đầu vào: apartIdPattern — pattern tìm kiếm LIKE
     * Đầu ra: List<HouseReg> các đăng ký khớp pattern
     */
    public List<HouseReg> findByApartmentIdLike(String apartIdPattern) {
        try (Session session = DB_manager.getFactory().openSession()) {
            var q = session.createQuery(
                    "FROM HouseReg WHERE apartment.apartId ILIKE :pattern AND deleteAt IS NULL",
                    HouseReg.class);
            q.setParameter("pattern", "%" + apartIdPattern + "%");
            return q.list();
        }
    }

    /**
     * Thêm đăng ký căn hộ mới.
     * Đầu vào: houseReg — entity HouseReg
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void addHouseReg(HouseReg houseReg) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(houseReg);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Cập nhật đăng ký căn hộ.
     * Đầu vào: houseReg — entity cần cập nhật
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void updateHouseReg(HouseReg houseReg) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(houseReg);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Xóa mềm đăng ký căn hộ (set deleteAt = now).
     * Đầu vào: houseReg — entity cần xóa
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void deleteHouseReg(HouseReg houseReg) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            HouseReg managed = session.merge(houseReg);
            managed.setDeleteAt(Instant.now());
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Xóa mềm đăng ký căn hộ theo mã.
     * Đầu vào: houseId — mã đăng ký
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void deleteByHouseId(String houseId) {
        HouseReg house = findByHouseId(houseId);
        if (house != null) {
            deleteHouseReg(house);
        }
    }

    /**
     * Kiểm tra mã house đã tồn tại chưa (và chưa bị xóa).
     * Đầu vào: houseId — mã cần kiểm tra
     * Đầu ra: true nếu tồn tại, false nếu không
     */
    public boolean exists(String houseId) {
        return findByHouseId(houseId) != null;
    }
}