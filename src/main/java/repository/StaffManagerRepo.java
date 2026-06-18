package repository;

import database.DB_manager;
import models.Staff;
import models.StaffDetail;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StaffManagerRepo {

    /**
     * Lấy tất cả nhân viên chưa bị xóa (delete_at IS NULL).
     * Đầu vào: không có
     * Đầu ra: List<Staff> chưa bị xóa mềm
     */
    public List<Staff> findAll() {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                "FROM Staff WHERE deleteAt IS NULL", Staff.class).list();
        }
    }

    /**
     * Lấy tất cả nhân viên chưa bị xóa theo danh sách roles được phép.
     * Đầu vào: roles — danh sách role mà user hiện tại được quyền quản lý
     * Đầu ra: List<Staff> có role nằm trong danh sách và chưa bị xóa
     */
    public List<Staff> findAllByRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) return List.of();
        try (Session session = DB_manager.getFactory().openSession()) {
            var q = session.createQuery(
                "FROM Staff WHERE role IN (:roles) AND deleteAt IS NULL", Staff.class);
            q.setParameterList("roles", roles);
            return q.list();
        }
    }

    /**
     * Tìm nhân viên theo staffId (exact match).
     * Đầu vào: staffId — mã nhân viên cần tìm
     * Đầu ra: Staff hoặc null nếu không tìm thấy
     */
    public Staff findById(String staffId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.get(Staff.class, staffId);
        }
    }

    /**
     * Tìm nhân viên theo staffId (LIKE) — chưa bị xóa.
     * Đầu vào: keyword — từ khóa tìm kiếm (sẽ match partial staffId)
     * Đầu ra: List<Staff> có staffId chứa keyword
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
     * Tìm nhân viên theo staffId (LIKE) trong phạm vi các role được phép.
     * Đầu vào: roles — danh sách role được phép; keyword — từ khóa tìm theo mã NV
     * Đầu ra: List<Staff> có staffId chứa keyword và role nằm trong danh sách
     */
    public List<Staff> findByRolesAndIdLike(List<String> roles, String keyword) {
        if (roles == null || roles.isEmpty()) return List.of();
        try (Session session = DB_manager.getFactory().openSession()) {
            var q = session.createQuery(
                "FROM Staff WHERE staffId ILIKE :kw AND role IN (:roles) AND deleteAt IS NULL", Staff.class);
            q.setParameter("kw", "%" + keyword + "%");
            q.setParameterList("roles", roles);
            return q.list();
        }
    }

    /**
     * Tìm nhân viên theo tên (join StaffDetail) — chưa bị xóa.
     * Đầu vào: name — từ khóa tìm theo tên nhân viên
     * Đầu ra: List<Staff> có tên chứa keyword
     */
    public List<Staff> findByName(String name) {
        try (Session session = DB_manager.getFactory().openSession()) {
            var q = session.createQuery(
                "SELECT s FROM Staff s JOIN StaffDetail sd ON sd.staff = s " +
                "WHERE sd.name ILIKE :name AND s.deleteAt IS NULL", Staff.class);
            q.setParameter("name", "%" + name + "%");
            return q.list();
        }
    }

    /**
     * Tìm nhân viên theo tên (join StaffDetail) trong phạm vi các role được phép.
     * Đầu vào: roles — danh sách role được phép; name — từ khóa tìm theo tên
     * Đầu ra: List<Staff> có tên chứa keyword và role nằm trong danh sách
     */
    public List<Staff> findByRolesAndName(List<String> roles, String name) {
        if (roles == null || roles.isEmpty()) return List.of();
        try (Session session = DB_manager.getFactory().openSession()) {
            var q = session.createQuery(
                "SELECT s FROM Staff s JOIN StaffDetail sd ON sd.staff = s " +
                "WHERE sd.name ILIKE :name AND s.role IN (:roles) AND s.deleteAt IS NULL", Staff.class);
            q.setParameter("name", "%" + name + "%");
            q.setParameterList("roles", roles);
            return q.list();
        }
    }

    /**
     * Tìm nhân viên theo role (exact match) — chưa bị xóa.
     * Đầu vào: role — role cần lọc
     * Đầu ra: List<Staff> có role khớp và chưa bị xóa
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
     * Lấy StaffDetail theo staffId.
     * Đầu vào: staffId — mã nhân viên
     * Đầu ra: StaffDetail hoặc null nếu không tìm thấy
     */
    public StaffDetail findStaffDetailByStaffId(String staffId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            Staff staff = session.get(Staff.class, staffId);
            if (staff == null) return null;
            return session.get(StaffDetail.class, staff);
        }
    }

    /**
     * Lấy batch StaffDetail cho danh sách staffId (tối ưu N+1 query).
     * Đầu vào: staffIds — danh sách mã nhân viên
     * Đầu ra: Map<String, String> mapping staffId → tên nhân viên
     */
    public Map<String, String> findStaffNameMap(List<String> staffIds) {
        if (staffIds == null || staffIds.isEmpty()) return Collections.emptyMap();
        try (Session session = DB_manager.getFactory().openSession()) {
            List<StaffDetail> details = session.createQuery(
                "FROM StaffDetail sd WHERE sd.staff.staffId IN (:ids)", StaffDetail.class)
                .setParameterList("ids", staffIds)
                .list();
            return details.stream().collect(Collectors.toMap(
                sd -> sd.getStaff().getStaffId(),
                StaffDetail::getName,
                (a, b) -> a
            ));
        }
    }

    /**
     * Thêm nhân viên mới (Staff + StaffDetail trong cùng transaction).
     * Đầu vào: staff — entity Staff; detail — entity StaffDetail
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void addStaff(Staff staff, StaffDetail detail) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(staff);
            detail.setStaff(staff);
            session.persist(detail);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Đổi mật khẩu nhân viên.
     * Đầu vào: staff — entity Staff cần đổi; newPassword — mật khẩu mới
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void updatePassword(Staff staff, String newPassword) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            Staff managed = session.merge(staff);
            managed.setPassword(newPassword);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Đổi role nhân viên.
     * Đầu vào: staff — entity Staff cần đổi; newRole — role mới
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void updateRole(Staff staff, String newRole) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            Staff managed = session.merge(staff);
            managed.setRole(newRole);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Xóa mềm nhân viên — set delete_at = now().
     * Đầu vào: staff — entity Staff cần xóa
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
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }
}
