package services;

import models.Apartment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.ApartmentRepo;
import user.AuthManager;

import java.util.List;

/**
 * Service quản lý căn hộ (apartment).
 * Xử lý logic nghiệp vụ liên quan đến căn hộ, kiểm tra quyền hạn.
 */
public class ApartmentServices {

    private final ApartmentRepo apartmentRepo;
    private static final Logger logger = LoggerFactory.getLogger(ApartmentServices.class);

    public ApartmentServices(ApartmentRepo apartmentRepo) {
        this.apartmentRepo = apartmentRepo;
    }

    /**
     * Lấy tất cả căn hộ chưa bị xóa.
     * Đầu vào: không có
     * Đầu ra: List<Apartment> tất cả căn hộ
     */
    public List<Apartment> findAll() {
        try {
            return apartmentRepo.findAll();
        } catch (Exception e) {
            logger.error("findAll apartments failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy căn hộ theo mã.
     * Đầu vào: apartId — mã căn hộ
     * Đầu ra: Apartment hoặc null nếu không tìm thấy
     */
    public Apartment findById(String apartId) {
        try {
            return apartmentRepo.findByApartId(apartId);
        } catch (Exception e) {
            logger.error("findById apartment failed for apartId={}: {}", apartId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lấy tất cả căn hộ của một tòa nhà.
     * Đầu vào: houseId — mã tòa nhà
     * Đầu ra: List<Apartment> các căn hộ trong tòa nhà
     */
    public List<Apartment> findByHouseId(String houseId) {
        try {
            return apartmentRepo.findByHouseId(houseId);
        } catch (Exception e) {
            logger.error("findByHouseId failed for houseId={}: {}", houseId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy căn hộ theo số phòng.
     * Đầu vào: roomNumber — số phòng
     * Đầu ra: List<Apartment> các căn hộ có số phòng khớp
     */
    public List<Apartment> findByRoomNumber(String roomNumber) {
        try {
            return apartmentRepo.findByRoomNumber(roomNumber);
        } catch (Exception e) {
            logger.error("findByRoomNumber failed for roomNumber={}: {}", roomNumber, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Tìm căn hộ trong khoảng diện tích.
     * Đầu vào: minArea, maxArea — giới hạn diện tích
     * Đầu ra: List<Apartment> các căn hộ trong khoảng
     */
    public List<Apartment> findByAreaRange(double minArea, double maxArea) {
        try {
            return apartmentRepo.findByAreaRange(minArea, maxArea);
        } catch (Exception e) {
            logger.error("findByAreaRange failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Thêm căn hộ mới — chỉ ADMIN hoặc người có quyền quản lý.
     * Đầu vào: apartment — entity Apartment mới
     * Đầu ra: true nếu thêm thành công, false nếu thất bại hoặc không có quyền
     */
    public boolean addApartment(Apartment apartment) {
        if (!AuthManager.hasAdminRole()) {
            logger.warn("User {} không có quyền thêm căn hộ",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }
        if (apartment == null || apartment.getApartId() == null || apartment.getApartId().isBlank()) {
            logger.warn("Cannot add apartment: invalid apartment data");
            return false;
        }
        try {
            apartmentRepo.addApartment(apartment);
            logger.info("Added new apartment: {}", apartment.getApartId());
            return true;
        } catch (Exception e) {
            logger.error("addApartment failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Cập nhật thông tin căn hộ — chỉ ADMIN hoặc người có quyền.
     * Đầu vào: apartment — entity cần cập nhật
     * Đầu ra: true nếu thành công, false nếu thất bại
     */
    public boolean updateApartment(Apartment apartment) {
        if (!AuthManager.hasAdminRole()) {
            logger.warn("User {} không có quyền cập nhật căn hộ",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }
        if (apartment == null || apartment.getApartId() == null) {
            logger.warn("Cannot update apartment: invalid apartment data");
            return false;
        }
        try {
            apartmentRepo.updateApartment(apartment);
            logger.info("Updated apartment: {}", apartment.getApartId());
            return true;
        } catch (Exception e) {
            logger.error("updateApartment failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Xóa mềm căn hộ (set deleteAt = now) — chỉ ADMIN.
     * Đầu vào: apartment — entity cần xóa
     * Đầu ra: true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteApartment(Apartment apartment) {
        if (!AuthManager.hasAdminRole()) {
            logger.warn("User {} không có quyền xóa căn hộ",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }
        if (apartment == null || apartment.getApartId() == null) {
            logger.warn("Cannot delete apartment: invalid apartment data");
            return false;
        }
        try {
            apartmentRepo.deleteApartment(apartment);
            logger.info("Deleted apartment: {}", apartment.getApartId());
            return true;
        } catch (Exception e) {
            logger.error("deleteApartment failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Xóa mềm căn hộ theo mã.
     * Đầu vào: apartId — mã căn hộ
     * Đầu ra: true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteByApartId(String apartId) {
        if (!AuthManager.hasAdminRole()) {
            return false;
        }
        try {
            apartmentRepo.deleteByApartId(apartId);
            logger.info("Deleted apartment by ID: {}", apartId);
            return true;
        } catch (Exception e) {
            logger.error("deleteByApartId failed for {}: {}", apartId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Kiểm tra căn hộ đã tồn tại chưa.
     * Đầu vào: apartId — mã căn hộ
     * Đầu ra: true nếu tồn tại (chưa xóa), false nếu không
     */
    public boolean exists(String apartId) {
        try {
            return apartmentRepo.findByApartId(apartId) != null;
        } catch (Exception e) {
            logger.error("exists check failed: {}", e.getMessage(), e);
            return false;
        }
    }
}