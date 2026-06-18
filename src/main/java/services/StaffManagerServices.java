package services;

import models.Staff;
import models.StaffDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.StaffManagerRepo;
import user.AuthManager;

import java.util.List;
import java.util.Map;

public class StaffManagerServices {

    private final StaffManagerRepo repo;
    private static final Logger logger = LoggerFactory.getLogger(StaffManagerServices.class);

    public StaffManagerServices(StaffManagerRepo repo) {
        this.repo = repo;
    }

    /**
     * Lấy tất cả nhân viên theo quyền hạn của user hiện tại.
     * - ADMIN: thấy tất cả staff
     * - RESIDENT: chỉ thấy staff role RESIDENT
     * - FINANCE: chỉ thấy staff role FINANCE
     * Đầu vào: không có
     * Đầu ra: List<Staff> trong phạm vi quyền quản lý
     */
    public List<Staff> findAll() {
        if (!AuthManager.hasStaffManagerRole()) {
            logger.warn("Unauthorized findAll staff");
            return List.of();
        }
        List<String> roles = AuthManager.getManageableRoles();
        return repo.findAllByRoles(roles);
    }

    /**
     * Tìm kiếm staff theo keyword trong phạm vi quyền hạn.
     * - Nếu keyword chỉ chứa chữ cái → tìm theo tên (join StaffDetail)
     * - Ngược lại → tìm theo mã nhân viên (LIKE)
     * Đầu vào: keyword — từ khóa tìm kiếm (mã NV hoặc tên)
     * Đầu ra: List<Staff> kết quả tìm kiếm trong phạm vi quyền
     */
    public List<Staff> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        List<String> roles = AuthManager.getManageableRoles();
        boolean isOnlyLetters = keyword.matches("^\\p{L}[\\p{L} ]*$");
        if (isOnlyLetters) {
            return repo.findByRolesAndName(roles, keyword);
        }
        return repo.findByRolesAndIdLike(roles, keyword);
    }

    /**
     * Tìm nhân viên theo role cụ thể.
     * Đầu vào: role — role cần lọc
     * Đầu ra: List<Staff> có role khớp và chưa bị xóa
     */
    public List<Staff> findByRole(String role) {
        return repo.findByRole(role);
    }

    /**
     * Lấy map staffId → tên nhân viên cho danh sách staff.
     * Dùng để hiển thị tên trong bảng thay vì chỉ hiện mã NV.
     * Đầu vào: staffList — danh sách Staff cần lấy tên
     * Đầu ra: Map<String, String> mapping staffId → name
     */
    public Map<String, String> getStaffNameMap(List<Staff> staffList) {
        List<String> ids = staffList.stream().map(Staff::getStaffId).toList();
        return repo.findStaffNameMap(ids);
    }

    /**
     * Thêm nhân viên mới.
     * Kiểm tra: user phải có quyền quản lý, role của staff mới phải nằm trong
     * phạm vi role mà user được quyền quản lý, và mã NV chưa tồn tại.
     * Đầu vào: staff — entity Staff mới; detail — entity StaffDetail mới
     * Đầu ra: true nếu thêm thành công, false nếu thất bại
     */
    public boolean addStaff(Staff staff, StaffDetail detail) {
        if (!AuthManager.hasStaffManagerRole()) return false;
        // Validate: role của staff mới phải nằm trong phạm vi quản lý
        List<String> manageableRoles = AuthManager.getManageableRoles();
        if (!manageableRoles.contains(staff.getRole())) {
            logger.warn("User {} không có quyền thêm staff với role {}",
                    AuthManager.getCurrentUser().getUserId(), staff.getRole());
            return false;
        }
        if (repo.findById(staff.getStaffId()) != null) {
            logger.warn("Staff {} already exists", staff.getStaffId());
            return false;
        }
        try {
            repo.addStaff(staff, detail);
            return true;
        } catch (Exception e) {
            logger.error("addStaff failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Đổi mật khẩu nhân viên.
     * Kiểm tra: user phải có quyền quản lý staff.
     * Đầu vào: staff — entity Staff cần đổi MK; newPassword — mật khẩu mới
     * Đầu ra: true nếu thành công, false nếu thất bại
     */
    public boolean updatePassword(Staff staff, String newPassword) {
        if (!AuthManager.hasStaffManagerRole()) return false;
        try {
            repo.updatePassword(staff, newPassword);
            return true;
        } catch (Exception e) {
            logger.error("updatePassword failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Đổi role nhân viên — chỉ ADMIN mới được phép.
     * Đầu vào: staff — entity Staff cần đổi role; newRole — role mới
     * Đầu ra: true nếu thành công, false nếu thất bại hoặc không có quyền
     */
    public boolean updateRole(Staff staff, String newRole) {
        if (!AuthManager.hasAdminRole()) return false;
        try {
            repo.updateRole(staff, newRole);
            return true;
        } catch (Exception e) {
            logger.error("updateRole failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Xóa mềm nhân viên (set delete_at = now).
     * Kiểm tra: user phải có quyền quản lý, role của staff bị xóa phải nằm
     * trong phạm vi quản lý, và không cho xóa chính mình.
     * Đầu vào: staff — entity Staff cần xóa
     * Đầu ra: true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteStaff(Staff staff) {
        if (!AuthManager.hasStaffManagerRole()) return false;
        // Không cho tự xóa chính mình
        if (staff.getStaffId().equals(AuthManager.getCurrentUser().getUserId())) {
            logger.warn("Cannot delete current logged-in user");
            return false;
        }
        // Validate: role của staff bị xóa phải nằm trong phạm vi quản lý
        List<String> manageableRoles = AuthManager.getManageableRoles();
        if (!manageableRoles.contains(staff.getRole())) {
            logger.warn("User {} không có quyền xóa staff role {}",
                    AuthManager.getCurrentUser().getUserId(), staff.getRole());
            return false;
        }
        try {
            repo.deleteStaff(staff);
            return true;
        } catch (Exception e) {
            logger.error("deleteStaff failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Giả lập gửi email thông tin tài khoản cho nhân viên mới.
     * Chỉ log thông tin và trả về true (demo, không gửi thật).
     * Đầu vào: email — địa chỉ email nhân viên; staff — entity Staff chứa thông tin tài khoản
     * Đầu ra: true (luôn thành công trong demo)
     */
    public boolean sendAccountEmail(String email, Staff staff) {
        logger.info("=== [DEMO] Gửi email thông tin tài khoản ===");
        logger.info("Đến: {}", email);
        logger.info("Mã nhân viên: {}", staff.getStaffId());
        logger.info("Vai trò: {}", staff.getRole());
        logger.info("=== [DEMO] Email đã được gửi thành công ===");
        return true;
    }
}
