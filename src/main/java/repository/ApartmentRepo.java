package repository;

import database.DB_manager;
import models.Apartment;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.Instant;
import java.util.List;

public class ApartmentRepo {

    /**
     * Lấy tất cả căn hộ chưa bị xóa.
     * Đầu vào: không có
     * Đầu ra: List<Apartment> tất cả căn hộ
     */
    public List<Apartment> findAll() {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM Apartment WHERE deleteAt IS NULL", Apartment.class).list();
        }
    }

    /**
     * Lấy căn hộ theo mã căn hộ.
     * Đầu vào: apartId — mã căn hộ
     * Đầu ra: Apartment hoặc null
     */
    public Apartment findByApartId(String apartId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            Apartment apt = session.get(Apartment.class, apartId);
            if (apt != null && apt.getDeleteAt() != null) return null;
            return apt;
        }
    }

    /**
     * Lấy tất cả căn hộ của một tòa nhà.
     * Đầu vào: houseId — mã tòa nhà
     * Đầu ra: List<Apartment> các căn hộ trong tòa nhà
     */
    public List<Apartment> findByHouseId(String houseId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            var q = session.createQuery(
                    "FROM Apartment WHERE houseId = :houseId AND deleteAt IS NULL", Apartment.class);
            q.setParameter("houseId", houseId);
            return q.list();
        }
    }

    /**
     * Lấy căn hộ theo số phòng.
     * Đầu vào: roomNumber — số phòng
     * Đầu ra: List<Apartment> các căn hộ có số phòng khớp
     */
    public List<Apartment> findByRoomNumber(String roomNumber) {
        try (Session session = DB_manager.getFactory().openSession()) {
            var q = session.createQuery(
                    "FROM Apartment WHERE roomNumber = :roomNumber AND deleteAt IS NULL",
                    Apartment.class);
            q.setParameter("roomNumber", roomNumber);
            return q.list();
        }
    }

    /**
     * Tìm căn hộ theo khoảng diện tích.
     * Đầu vào: minArea, maxArea — giới hạn diện tích
     * Đầu ra: List<Apartment> các căn hộ trong khoảng diện tích
     */
    public List<Apartment> findByAreaRange(double minArea, double maxArea) {
        try (Session session = DB_manager.getFactory().openSession()) {
            var q = session.createQuery(
                    "FROM Apartment WHERE area >= :minArea AND area <= :maxArea AND deleteAt IS NULL",
                    Apartment.class);
            q.setParameter("minArea", minArea);
            q.setParameter("maxArea", maxArea);
            return q.list();
        }
    }

    /**
     * Thêm căn hộ mới.
     * Đầu vào: apartment — entity Apartment
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void addApartment(Apartment apartment) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(apartment);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Cập nhật thông tin căn hộ.
     * Đầu vào: apartment — entity cần cập nhật
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void updateApartment(Apartment apartment) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(apartment);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Xóa mềm căn hộ (set deleteAt = now).
     * Đầu vào: apartment — entity cần xóa
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void deleteApartment(Apartment apartment) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            Apartment managed = session.merge(apartment);
            managed.setDeleteAt(Instant.now());
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Xóa mềm căn hộ theo mã.
     * Đầu vào: apartId — mã căn hộ
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void deleteByApartId(String apartId) {
        Apartment apt = findByApartId(apartId);
        if (apt != null) {
            deleteApartment(apt);
        }
    }
}