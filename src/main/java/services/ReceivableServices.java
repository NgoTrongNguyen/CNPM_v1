package services;

import models.Receivable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.ReceivableRepo;
import user.AuthManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service quản lý khoản thu định kỳ (Receivable).
 * Xử lý logic nghiệp vụ cho khoản thu: thêm, sửa, xóa, tìm kiếm.
 */
public class ReceivableServices {

    public enum ReceiCategory {
        SERVICE_ELECTRIC,
        SERVICE_WATER,
        SERVICE_OTHER,
        CONTRIBUTION
    }

    public static final int DEFAULT_PAY_DEADLINE_DAYS = 10;
    public static final long DEFAULT_ELECTRIC_PRICE_PER_UNIT = 3500;
    public static final long DEFAULT_WATER_PRICE_PER_UNIT = 15000;

    private final ReceivableRepo receivableRepo;
    private static final Logger logger = LoggerFactory.getLogger(ReceivableServices.class);

    public ReceivableServices(ReceivableRepo receivableRepo) {
        this.receivableRepo = receivableRepo;
    }

    /**
     * Lấy tất cả khoản thu định kỳ chưa bị xóa.
     * Đầu vào: không có
     * Đầu ra: List<Receivable> tất cả khoản thu
     */
    public List<Receivable> findAll() {
        try {
            return receivableRepo.findAll();
        } catch (Exception e) {
            logger.error("findAll receivables failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy khoản thu theo mã.
     * Đầu vào: receiId — mã khoản thu
     * Đầu ra: Receivable hoặc null nếu không tìm thấy
     */
    public Receivable findById(String receiId) {
        try {
            return receivableRepo.findByReceiId(receiId);
        } catch (Exception e) {
            logger.error("findById receivable failed: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lấy tất cả khoản thu bắt buộc.
     * Đầu vào: không có
     * Đầu ra: List<Receivable> các khoản bắt buộc
     */
    public List<Receivable> findMandatory() {
        try {
            return receivableRepo.findMandatory();
        } catch (Exception e) {
            logger.error("findMandatory failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy tất cả khoản thu không bắt buộc (tùy chọn).
     * Đầu vào: không có
     * Đầu ra: List<Receivable> các khoản không bắt buộc
     */
    public List<Receivable> findOptional() {
        try {
            return receivableRepo.findOptional();
        } catch (Exception e) {
            logger.error("findOptional failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy tất cả khoản thu có giá cố định.
     * Đầu vào: không có
     * Đầu ra: List<Receivable> các khoản cố định
     */
    public List<Receivable> findFixed() {
        try {
            return receivableRepo.findFixed();
        } catch (Exception e) {
            logger.error("findFixed failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Tìm khoản thu theo tên.
     * Đầu vào: name — từ khóa tìm kiếm
     * Đầu ra: List<Receivable> các khoản khớp pattern
     */
    public List<Receivable> findByName(String name) {
        try {
            return receivableRepo.findByName(name);
        } catch (Exception e) {
            logger.error("findByName failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Tìm khoản thu trong khoảng giá.
     * Đầu vào: minPrice, maxPrice — giới hạn giá
     * Đầu ra: List<Receivable> các khoản trong khoảng
     */
    public List<Receivable> findByPriceRange(long minPrice, long maxPrice) {
        try {
            return receivableRepo.findByPriceRange(minPrice, maxPrice);
        } catch (Exception e) {
            logger.error("findByPriceRange failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Thêm khoản thu mới — chỉ FINANCE hoặc ADMIN.
     * Đầu vào: receivable — entity Receivable mới
     * Đầu ra: true nếu thêm thành công, false nếu thất bại
     */
    public boolean addReceivable(Receivable receivable) {
        if (!AuthManager.hasFinanceRole()) {
            logger.warn("User {} không có quyền thêm khoản thu",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }
        if (receivable == null || receivable.getReceiId() == null || receivable.getReceiId().isBlank()) {
            logger.warn("Cannot add receivable: invalid data");
            return false;
        }
        try {
            receivableRepo.addReceivable(receivable);
            logger.info("Added new receivable: {}", receivable.getReceiId());
            return true;
        } catch (Exception e) {
            logger.error("addReceivable failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Cập nhật khoản thu — chỉ FINANCE hoặc ADMIN.
     * Đầu vào: receivable — entity cần cập nhật
     * Đầu ra: true nếu thành công, false nếu thất bại
     */
    public boolean updateReceivable(Receivable receivable) {
        if (!AuthManager.hasFinanceRole()) {
            logger.warn("User {} không có quyền cập nhật khoản thu",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }
        try {
            receivableRepo.updateReceivable(receivable);
            logger.info("Updated receivable: {}", receivable.getReceiId());
            return true;
        } catch (Exception e) {
            logger.error("updateReceivable failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Xóa mềm khoản thu — chỉ FINANCE hoặc ADMIN.
     * Đầu vào: receivable — entity cần xóa
     * Đầu ra: true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteReceivable(Receivable receivable) {
        if (!AuthManager.hasFinanceRole()) {
            logger.warn("User {} không có quyền xóa khoản thu",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }
        try {
            receivableRepo.deleteReceivable(receivable);
            logger.info("Deleted receivable: {}", receivable.getReceiId());
            return true;
        } catch (Exception e) {
            logger.error("deleteReceivable failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Kiểm tra khoản thu đã tồn tại chưa.
     * Đầu vào: receiId — mã khoản thu
     * Đầu ra: true nếu tồn tại, false nếu không
     */
    public boolean exists(String receiId) {
        try {
            return receivableRepo.exists(receiId);
        } catch (Exception e) {
            logger.error("exists check failed: {}", e.getMessage(), e);
            return false;
        }
    }

    public Instant calculateDefaultDeadline() {
        return Instant.now().plus(DEFAULT_PAY_DEADLINE_DAYS, ChronoUnit.DAYS);
    }

    public long calculatePrice(ReceiCategory category, double quantity, long manualAmount) {
        return switch (category) {
            case SERVICE_ELECTRIC -> (long) (quantity * DEFAULT_ELECTRIC_PRICE_PER_UNIT);
            case SERVICE_WATER -> (long) (quantity * DEFAULT_WATER_PRICE_PER_UNIT);
            case SERVICE_OTHER, CONTRIBUTION -> manualAmount;
        };
    }

    public Receivable createReceivable(String receiId, String receiName, ReceiCategory category,
                                      boolean mandatory, double quantity, long manualAmount,
                                      String description) {
        if (!AuthManager.hasManagerRole()) {
            throw new SecurityException("Bạn không có quyền tạo khoản thu mới.");
        }
        if (receiId == null || receiId.isBlank()) {
            throw new IllegalArgumentException("Mã khoản thu không được để trống.");
        }
        if (receiName == null || receiName.isBlank()) {
            throw new IllegalArgumentException("Tên khoản thu không được để trống.");
        }

        long price = calculatePrice(category, quantity, manualAmount);
        Receivable receivable = new Receivable();
        receivable.setReceiId(receiId);
        receivable.setReceiName(receiName);
        receivable.setPrice(price);
        receivable.setMandatory(mandatory);
        receivable.setDescription(description);

        addReceivable(receivable);
        return receivable;
    }
}