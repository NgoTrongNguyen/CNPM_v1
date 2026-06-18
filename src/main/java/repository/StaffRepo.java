package repository;

import database.DB_manager;
import models.Staff;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.Instant;
import java.util.List;

/**
 * Repository cơ bản cho entity Staff.
 * Xử lý các thao tác CRUD đơn giản với bảng staff.
 *
 * Lưu ý:
 * - StaffManagerRepo dùng cho các truy vấn phức tạp, lọc theo role, tìm kiếm,...
 * - StaffRepo dùng cho các thao tác cơ bản (CRUD)
 */
public class StaffRepo {

    /**
     * Lấy tất cả nhân viên (kể cả đã bị soft-delete).
     * Đầu vào: không có
     * Đầu ra: List<Staff> tất cả nhân viên
     */
    public List<Staff> findAll() {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery("FROM Staff", Staff.class).list();
        }
    }

    /**
     * Lấy tất cả nhân viên chưa bị xóa (deleteAt IS NULL).
     * Đầu vào: không có
     * Đầu ra: List<Staff> nhân viên còn hoạt động
     */
    public List<Staff> findAllActive() {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM Staff WHERE deleteAt IS NULL", Staff.class).list();
        }
    }

    /**
     * Lấy nhân viên theo mã staffId.
     * Đầu vào: staffId — mã nhân viên
     * Đầu ra: Staff hoặc null nếu không tìm thấy
     */
    public Staff findByStaffId(String staffId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.get(Staff.class, staffId);
        }
    }

    /**
     * Tìm nhân viên theo mã (ILIKE pattern), chưa bị xóa.
     * Đầu vào: keyword — từ khóa tìm theo mã nhân viên
     * Đầu ra: List<Staff> nhân viên khớp pattern
     */
    public List<Staff> findByIdLike(String keyword) {
        try (Session session = DB_manager.getFactory().openSession()) {
            var q = session.createQuery(
                    "FROM Staff WHERE staffId ILIKE :kw AND deleteAt IS NULL", Staff.class);
            q.setParameter("kw", "%" + keyword + "%");
            return q.list();
        }
    }

    /**
     * Tìm nhân viên theo role, chưa bị xóa.
     * Đầu vào: role — vai trò cần tìm
     * Đầu ra: List<Staff> nhân viên có role khớp
     */
    public List<Staff> findByRole(String role) {
        try (Session session = DB_manager.getFactory().openSession()) {
            var q = session.createQuery(
                    "FROM Staff WHERE role = :role AND deleteAt IS NULL", Staff.class);
            q.setParameter("role", role);
            return q.list();
        }
    }

    /**
     * Kiểm tra mã nhân viên đã tồn tại chưa.
     * Đầu vào: staffId — mã cần kiểm tra
     * Đầu ra: true nếu tồn tại, false nếu không
     */
    public boolean exists(String staffId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            Staff staff = session.get(Staff.class, staffId);
            return staff != null;
        }
    }

    /**
     * Thêm nhân viên mới.
     * Đầu vào: staff — entity Staff cần thêm
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void addStaff(Staff staff) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(staff);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Cập nhật thông tin nhân viên.
     * Đầu vào: staff — entity cần cập nhật
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void updateStaff(Staff staff) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(staff);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Cập nhật mật khẩu nhân viên.
     * Đầu vào: staffId — mã nhân viên; newPassword — mật khẩu mới
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void updatePassword(String staffId, String newPassword) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            Staff staff = session.get(Staff.class, staffId);
            if (staff != null) {
                staff.setPassword(newPassword);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Cập nhật role nhân viên.
     * Đầu vào: staffId — mã nhân viên; newRole — role mới
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void updateRole(String staffId, String newRole) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            Staff staff = session.get(Staff.class, staffId);
            if (staff != null) {
                staff.setRole(newRole);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Xóa mềm nhân viên (set deleteAt = now).
     * Đầu vào: staff — entity cần xóa
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void deleteStaff(Staff staff) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            Staff managed = session.merge(staff);
            managed.setDeleteAt(Instant.now());
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Xóa mềm nhân viên theo mã.
     * Đầu vào: staffId — mã nhân viên
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void deleteByStaffId(String staffId) {
        Staff staff = findByStaffId(staffId);
        if (staff != null) {
            deleteStaff(staff);
        }
    }
}