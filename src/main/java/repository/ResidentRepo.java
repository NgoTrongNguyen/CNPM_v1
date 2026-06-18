package repository;

import database.DB_manager;
import models.Resident;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class ResidentRepo {
    /**
     * Lấy tất cả cư dân chưa bị xóa (deleteAt IS NULL).
     * BUG FIX: Trước đây đang lọc deleteAt IS NOT NULL (lấy cái đã xóa)
     * Đầu vào: không có
     * Đầu ra: List<Resident> tất cả cư dân chưa xóa
     */
    public List<Resident> findAll() {
        try(Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery("FROM Resident WHERE deleteAt IS NULL", Resident.class).list();
        }
    }

    /**
     * Lấy cư dân theo mã.
     * Đầu vào: residentId — mã cư dân
     * Đầu ra: Resident hoặc null nếu không tìm thấy
     */
    public Resident findById(String residentId){
        try(Session session = DB_manager.getFactory().openSession()) {
            return  session.get(Resident.class, residentId);
        }
    }

    /**
     * Tìm cư dân theo tên (ILIKE pattern matching), chưa bị xóa.
     * Đầu vào: name — từ khóa tìm kiếm
     * Đầu ra: List<Resident> cư dân có tên khớp
     */
    public List<Resident> findWithName(String name) {
        try(Session session = DB_manager.getFactory().openSession()) {
            String search = "%"+name+"%";
            var query = session.createQuery("FROM Resident WHERE name ILIKE :name AND deleteAt IS NULL", Resident.class);
            query.setParameter("name", search);
            return query.list();
        }
    }

    /**
     * Tìm cư dân theo CCCD hoặc số điện thoại (ILIKE), chưa bị xóa.
     * Đầu vào: number — từ khóa tìm kiếm (CCCD hoặc SĐT)
     * Đầu ra: List<Resident> cư dân khớp
     */
    public List<Resident> findWithNumber(String number) {
        try(Session session = DB_manager.getFactory().openSession()) {
            String search = "%"+number+"%";
            var query = session.createQuery(
                    "FROM Resident WHERE (residentId ILIKE :info OR telephone ILIKE :info) AND deleteAt IS NULL",
                    Resident.class);
            return query.setParameter("info", search).list();
        }
    }

    /**
     * Tìm cư dân theo ngày sinh, chưa bị xóa.
     * Đầu vào: date — ngày sinh cần tìm
     * Đầu ra: List<Resident> cư dân có ngày sinh khớp
     */
    public List<Resident> findByDate(LocalDate date) {
        try(Session session = DB_manager.getFactory().openSession()) {
            var query = session.createQuery("FROM Resident WHERE birthday = :date AND deleteAt IS NULL", Resident.class);
            query.setParameter("date", date);
            return query.list();
        }
    }

    /**
     * Thêm cư dân mới.
     * Đầu vào: resident — entity Resident
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void addResident(Resident resident) {
        Transaction transaction = null;
        try(Session session = DB_manager.getFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(resident);
            transaction.commit();
        } catch(Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        }
    }

    /**
     * Xóa mềm cư dân (set deleteAt = now).
     * Đầu vào: resident — entity cần xóa
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void deleteResident(Resident resident) {
        Transaction transaction = null;
        try(Session session = DB_manager.getFactory().openSession()) {
            transaction = session.beginTransaction();
            Resident managed = session.merge(resident);
            managed.setDeleteAt(Instant.now());
            transaction.commit();
        }catch(Exception ex) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw ex;
        }
    }
}