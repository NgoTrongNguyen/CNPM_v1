package services;

import models.Staff;
import models.StaffDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.StaffDetailRepo;
import repository.StaffRepo;
import user.AuthManager;

public class StaffDetailServices {

    private final StaffDetailRepo detailRepo;
    private final StaffRepo staffRepo;
    private static final Logger logger = LoggerFactory.getLogger(StaffDetailServices.class);

    public StaffDetailServices(StaffDetailRepo detailRepo, StaffRepo staffRepo) {
        this.detailRepo = detailRepo;
        this.staffRepo  = staffRepo;
    }

    /** Lấy thông tin chi tiết của người dùng đang đăng nhập */
    public StaffDetail getMyDetail() {
        String id = AuthManager.getCurrentUser().getUserId();
        return detailRepo.findByStaffId(id);
    }

    /** Lấy Staff entity của người dùng đang đăng nhập */
    public Staff getMyStaff() {
        String id = AuthManager.getCurrentUser().getUserId();
        return staffRepo.findByStaffId(id);
    }

    /** Cập nhật tên và địa chỉ */
    public boolean updateDetail(StaffDetail detail) {
        try {
            detailRepo.updateDetail(detail);
            return true;
        } catch (Exception e) {
            logger.error("updateDetail failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Đổi mật khẩu — kiểm tra mật khẩu cũ trước khi cho đổi.
     * @return true nếu thành công, false nếu sai mật khẩu cũ
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        Staff staff = getMyStaff();
        if (staff == null) return false;
        if (!staff.getPassword().equals(oldPassword)) {
            logger.warn("changePassword: wrong old password for {}", staff.getStaffId());
            return false;
        }
        try {
            detailRepo.changePassword(staff, newPassword);
            return true;
        } catch (Exception e) {
            logger.error("changePassword failed: {}", e.getMessage(), e);
            return false;
        }
    }
}
