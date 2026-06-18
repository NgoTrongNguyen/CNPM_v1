package services;

import models.HouseRecei;
import models.HouseReg;
import models.Receivable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.HouseReceiRepo;
import user.AuthManager;

import java.time.Instant;
import java.util.List;

/**
 * Service quản lý khoản thu gắn với căn hộ (HouseRecei).
 * Xử lý logic nghiệp vụ: thêm, cập nhật, xóa, tìm kiếm khoản thu.
 */
public class HouseReceiServices {

    private final HouseReceiRepo houseReceiRepo;
    private static final Logger logger = LoggerFactory.getLogger(HouseReceiServices.class);

    public HouseReceiServices(HouseReceiRepo houseReceiRepo) {
        this.houseReceiRepo = houseReceiRepo;
    }

    /**
     * Lấy tất cả khoản thu chưa bị xóa.
     * Đầu vào: không có
     * Đầu ra: List<HouseRecei> tất cả khoản thu
     */
    public List<HouseRecei> findAll() {
        try {
            return houseReceiRepo.findAll();
        } catch (Exception e) {
            logger.error("findAll house_recei failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy tất cả khoản thu của một căn hộ.
     * Đầu vào: houseId — mã căn hộ
     * Đầu ra: List<HouseRecei> các khoản thu của căn hộ
     */
    public List<HouseRecei> findByHouseId(String houseId) {
        try {
            return houseReceiRepo.findByHouseId(houseId);
        } catch (Exception e) {
            logger.error("findByHouseId failed for houseId={}: {}", houseId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy tất cả khoản thu theo trạng thái thanh toán.
     * Đầu vào: status — true = đã thanh toán, false = chưa thanh toán
     * Đầu ra: List<HouseRecei> khoản thu khớp trạng thái
     */
    public List<HouseRecei> findByStatus(boolean status) {
        try {
            return houseReceiRepo.findByStatus(status);
        } catch (Exception e) {
            logger.error("findByStatus failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy tất cả khoản thu chưa thanh toán (overdue hoặc sắp tới hạn).
     * Đầu vào: không có
     * Đầu ra: List<HouseRecei> các khoản status = false
     */
    public List<HouseRecei> findUnpaid() {
        return findByStatus(false);
    }

    /**
     * Lấy khoản thu theo houseId và receivableId.
     * Đầu vào: houseId, receiId — khóa tổng hợp
     * Đầu ra: HouseRecei hoặc null
     */
    public HouseRecei findByCompositeId(String houseId, String receiId) {
        try {
            return houseReceiRepo.findByCompositeId(houseId, receiId);
        } catch (Exception e) {
            logger.error("findByCompositeId failed: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Thêm khoản thu mới — chỉ FINANCE hoặc ADMIN.
     * Đầu vào: houseRecei — entity HouseRecei
     * Đầu ra: true nếu thêm thành công, false nếu thất bại
     */
    public boolean addHouseRecei(HouseRecei houseRecei) {
        if (!AuthManager.hasFinanceRole()) {
            logger.warn("User {} không có quyền thêm khoản thu",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }
        if (houseRecei == null || houseRecei.getHouseReg() == null || houseRecei.getReceivable() == null) {
            logger.warn("Cannot add house_recei: invalid data");
            return false;
        }
        try {
            houseReceiRepo.addHouseRecei(houseRecei);
            logger.info("Added new house_recei: house={}, recei={}",
                    houseRecei.getHouseReg().getHouseId(),
                    houseRecei.getReceivable().getReceiId());
            return true;
        } catch (Exception e) {
            logger.error("addHouseRecei failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Cập nhật trạng thái thanh toán của khoản thu.
     * Đầu vào: houseId, receiId — khóa tổng hợp; status — true/false
     * Đầu ra: true nếu thành công, false nếu thất bại
     */
    public boolean updateStatus(String houseId, String receiId, boolean status) {
        if (!AuthManager.hasFinanceRole()) {
            logger.warn("User {} không có quyền cập nhật khoản thu",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }
        try {
            houseReceiRepo.updateStatus(houseId, receiId, status);
            logger.info("Updated house_recei status: house={}, recei={}, status={}",
                    houseId, receiId, status);
            return true;
        } catch (Exception e) {
            logger.error("updateStatus failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Cập nhật thông tin khoản thu (quantity, description, ...).
     * Đầu vào: houseRecei — entity cần cập nhật
     * Đầu ra: true nếu thành công, false nếu thất bại
     */
    public boolean updateHouseRecei(HouseRecei houseRecei) {
        if (!AuthManager.hasFinanceRole()) {
            logger.warn("User {} không có quyền cập nhật khoản thu",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }
        if (houseRecei == null) {
            return false;
        }
        try {
            houseReceiRepo.updateHouseRecei(houseRecei);
            logger.info("Updated house_recei: house={}, recei={}",
                    houseRecei.getHouseReg().getHouseId(),
                    houseRecei.getReceivable().getReceiId());
            return true;
        } catch (Exception e) {
            logger.error("updateHouseRecei failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Xóa mềm khoản thu.
     * Đầu vào: houseRecei — entity cần xóa
     * Đầu ra: true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteHouseRecei(HouseRecei houseRecei) {
        if (!AuthManager.hasFinanceRole()) {
            logger.warn("User {} không có quyền xóa khoản thu",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }
        if (houseRecei == null) {
            return false;
        }
        try {
            houseReceiRepo.deleteHouseRecei(houseRecei);
            logger.info("Deleted house_recei: house={}, recei={}",
                    houseRecei.getHouseReg().getHouseId(),
                    houseRecei.getReceivable().getReceiId());
            return true;
        } catch (Exception e) {
            logger.error("deleteHouseRecei failed: {}", e.getMessage(), e);
            return false;
        }
    }
}