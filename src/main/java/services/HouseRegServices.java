package services;

import models.HouseReg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.HouseRegRepo;
import user.AuthManager;

import java.util.List;

/**
 * Service quản lý đăng ký căn hộ (HouseReg).
 * Xử lý logic nghiệp vụ: thêm, sửa, xóa, tìm kiếm đăng ký căn hộ.
 */
public class HouseRegServices {

    private final HouseRegRepo houseRegRepo;
    private static final Logger logger = LoggerFactory.getLogger(HouseRegServices.class);

    public HouseRegServices(HouseRegRepo houseRegRepo) {
        this.houseRegRepo = houseRegRepo;
    }

    /**
     * Lấy tất cả đăng ký căn hộ chưa bị xóa.
     * Đầu vào: không có
     * Đầu ra: List<HouseReg> tất cả đăng ký
     */
    public List<HouseReg> findAll() {
        try {
            return houseRegRepo.findAll();
        } catch (Exception e) {
            logger.error("findAll house_reg failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy đăng ký căn hộ theo mã.
     * Đầu vào: houseId — mã đăng ký
     * Đầu ra: HouseReg hoặc null nếu không tìm thấy
     */
    public HouseReg findByHouseId(String houseId) {
        try {
            return houseRegRepo.findByHouseId(houseId);
        } catch (Exception e) {
            logger.error("findByHouseId failed: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lấy tất cả đăng ký của một căn hộ (apartment).
     * Đầu vào: apartId — mã căn hộ
     * Đầu ra: List<HouseReg> các đăng ký cho căn hộ
     */
    public List<HouseReg> findByApartmentId(String apartId) {
        try {
            return houseRegRepo.findByApartmentId(apartId);
        } catch (Exception e) {
            logger.error("findByApartmentId failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Tìm đăng ký theo apartment (LIKE pattern).
     * Đầu vào: apartIdPattern — pattern tìm kiếm
     * Đầu ra: List<HouseReg> đăng ký khớp pattern
     */
    public List<HouseReg> findByApartmentIdLike(String apartIdPattern) {
        try {
            return houseRegRepo.findByApartmentIdLike(apartIdPattern);
        } catch (Exception e) {
            logger.error("findByApartmentIdLike failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Thêm đăng ký căn hộ mới — chỉ ADMIN hoặc người có quyền.
     * Đầu vào: houseReg — entity HouseReg
     * Đầu ra: true nếu thêm thành công, false nếu thất bại
     */
    public boolean addHouseReg(HouseReg houseReg) {
        if (!AuthManager.hasAdminRole()) {
            logger.warn("User {} không có quyền thêm đăng ký căn hộ",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }
        if (houseReg == null || houseReg.getHouseId() == null || houseReg.getHouseId().isBlank()) {
            logger.warn("Cannot add house_reg: invalid data");
            return false;
        }
        try {
            houseRegRepo.addHouseReg(houseReg);
            logger.info("Added new house_reg: {}", houseReg.getHouseId());
            return true;
        } catch (Exception e) {
            logger.error("addHouseReg failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Cập nhật đăng ký căn hộ — chỉ ADMIN.
     * Đầu vào: houseReg — entity cần cập nhật
     * Đầu ra: true nếu thành công, false nếu thất bại
     */
    public boolean updateHouseReg(HouseReg houseReg) {
        if (!AuthManager.hasAdminRole()) {
            logger.warn("User {} không có quyền cập nhật đăng ký căn hộ",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }
        if (houseReg == null || houseReg.getHouseId() == null) {
            logger.warn("Cannot update house_reg: invalid data");
            return false;
        }
        try {
            houseRegRepo.updateHouseReg(houseReg);
            logger.info("Updated house_reg: {}", houseReg.getHouseId());
            return true;
        } catch (Exception e) {
            logger.error("updateHouseReg failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Xóa mềm đăng ký căn hộ — chỉ ADMIN.
     * Đầu vào: houseReg — entity cần xóa
     * Đầu ra: true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteHouseReg(HouseReg houseReg) {
        if (!AuthManager.hasAdminRole()) {
            logger.warn("User {} không có quyền xóa đăng ký căn hộ",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }
        if (houseReg == null || houseReg.getHouseId() == null) {
            logger.warn("Cannot delete house_reg: invalid data");
            return false;
        }
        try {
            houseRegRepo.deleteHouseReg(houseReg);
            logger.info("Deleted house_reg: {}", houseReg.getHouseId());
            return true;
        } catch (Exception e) {
            logger.error("deleteHouseReg failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Kiểm tra đăng ký căn hộ đã tồn tại chưa.
     * Đầu vào: houseId — mã cần kiểm tra
     * Đầu ra: true nếu tồn tại, false nếu không
     */
    public boolean exists(String houseId) {
        try {
            return houseRegRepo.exists(houseId);
        } catch (Exception e) {
            logger.error("exists check failed: {}", e.getMessage(), e);
            return false;
        }
    }
}