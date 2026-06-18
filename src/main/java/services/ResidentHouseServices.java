package services;

import models.HouseReg;
import models.Resident;
import models.ResidentHouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.ResidentHouseRepo;
import user.AuthManager;

import java.util.List;

/**
 * Service quản lý mối quan hệ cư dân-căn hộ (ResidentHouse).
 *
 * <p>Kiểm tra quyền hạn (ADMIN/MANAGER), validate dữ liệu đầu vào,
 * ghi nhật ký audit qua {@link AuditLogService} cho mọi thao tác ghi.</p>
 *
 * <h3>Quy tắc nghiệp vụ:</h3>
 * <ul>
 *   <li>Chỉ ADMIN hoặc MANAGER mới được thêm/xóa/cập nhật mối quan hệ.</li>
 *   <li>Mỗi cư dân chỉ xuất hiện 1 lần trong 1 căn hộ (composite key).</li>
 *   <li>Mỗi căn hộ chỉ có TỐI ĐA 1 chủ hộ (isMaster = true).
 *       Khi đặt chủ hộ mới, chủ hộ cũ sẽ bị hạ xuống thành viên.</li>
 * </ul>
 */
public class ResidentHouseServices {

    private final ResidentHouseRepo repo;
    private static final Logger logger = LoggerFactory.getLogger(ResidentHouseServices.class);

    public ResidentHouseServices(ResidentHouseRepo repo) {
        this.repo = repo;
    }

    // ══════════════════════════════════════════════════════════════
    // QUERIES — Không cần kiểm tra quyền vì chỉ đọc
    // ══════════════════════════════════════════════════════════════

    /**
     * Lấy tất cả mối quan hệ cư dân-căn hộ (cư dân & căn hộ đều chưa bị xóa).
     *
     * @return List<ResidentHouse> — rỗng nếu không có hoặc lỗi
     */
    public List<ResidentHouse> findAll() {
        try {
            return repo.findAll();
        } catch (Exception e) {
            logger.error("findAll resident_house failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy tất cả căn hộ mà một cư dân đang ở.
     *
     * @param residentId mã cư dân (CCCD)
     * @return List<ResidentHouse> — rỗng nếu cư dân chưa đăng ký căn hộ nào
     */
    public List<ResidentHouse> findHousesOfResident(String residentId) {
        if (residentId == null || residentId.isBlank()) {
            logger.warn("findHousesOfResident: residentId is blank");
            return List.of();
        }
        try {
            return repo.findByResidentId(residentId);
        } catch (Exception e) {
            logger.error("findHousesOfResident failed for residentId={}: {}", residentId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy tất cả cư dân đang sống trong một căn hộ.
     *
     * @param houseId mã hộ khẩu (house_reg.house_id)
     * @return List<ResidentHouse> — rỗng nếu căn hộ chưa có ai
     */
    public List<ResidentHouse> findResidentsInHouse(String houseId) {
        if (houseId == null || houseId.isBlank()) {
            logger.warn("findResidentsInHouse: houseId is blank");
            return List.of();
        }
        try {
            return repo.findByHouseId(houseId);
        } catch (Exception e) {
            logger.error("findResidentsInHouse failed for houseId={}: {}", houseId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Tìm chủ hộ (isMaster = true) của một căn hộ.
     *
     * @param houseId mã hộ khẩu
     * @return ResidentHouse của chủ hộ, hoặc null nếu căn hộ chưa có chủ hộ
     */
    public ResidentHouse findMasterOfHouse(String houseId) {
        if (houseId == null || houseId.isBlank()) return null;
        try {
            return repo.findMasterByHouseId(houseId);
        } catch (Exception e) {
            logger.error("findMasterOfHouse failed for houseId={}: {}", houseId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Kiểm tra cư dân có phải chủ hộ của căn hộ đó không.
     *
     * @param residentId mã cư dân
     * @param houseId    mã hộ khẩu
     * @return true nếu là chủ hộ
     */
    public boolean isMasterOfHouse(String residentId, String houseId) {
        try {
            ResidentHouse rh = repo.findByCompositeId(residentId, houseId);
            return rh != null && rh.isMaster();
        } catch (Exception e) {
            logger.error("isMasterOfHouse check failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Đếm số cư dân hiện có trong một căn hộ.
     *
     * @param houseId mã hộ khẩu
     * @return số cư dân (0 nếu chưa có ai hoặc lỗi)
     */
    public int countResidentsInHouse(String houseId) {
        return findResidentsInHouse(houseId).size();
    }

    // ══════════════════════════════════════════════════════════════
    // WRITE OPERATIONS — Cần quyền MANAGER hoặc ADMIN
    // ══════════════════════════════════════════════════════════════

    /**
     * Gán cư dân vào căn hộ.
     *
     * <p>Quy tắc:
     * <ul>
     *   <li>Người dùng phải có quyền MANAGER hoặc ADMIN.</li>
     *   <li>Cư dân và căn hộ đều phải hợp lệ (không null).</li>
     *   <li>Cặp (resident, house) chưa được gán trước đó.</li>
     * </ul>
     *
     * @param resident cư dân cần gán
     * @param house    căn hộ đích
     * @param isMaster true nếu gán làm chủ hộ
     * @return true nếu thành công, false nếu thất bại hoặc không đủ quyền
     */
    public boolean addResidentToHouse(Resident resident, HouseReg house, boolean isMaster) {
        // Kiểm tra quyền
        if (!AuthManager.hasManagerRole()) {
            logger.warn("User {} không có quyền gán cư dân vào căn hộ",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }

        // Validate đầu vào
        if (resident == null || resident.getResidentId() == null) {
            logger.warn("addResidentToHouse: resident is null or has no ID");
            return false;
        }
        if (house == null || house.getHouseId() == null) {
            logger.warn("addResidentToHouse: house is null or has no ID");
            return false;
        }

        try {
            // Tránh trùng lặp mối quan hệ — kiểm tra composite key
            if (repo.findByCompositeId(resident.getResidentId(), house.getHouseId()) != null) {
                logger.warn("Resident {} đã thuộc căn hộ {} rồi",
                        resident.getResidentId(), house.getHouseId());
                return false;
            }

            // Nếu gán làm chủ hộ, đảm bảo hạ chủ hộ cũ (nếu có)
            if (isMaster) {
                demoteCurrentMaster(house.getHouseId());
            }

            // Tạo mối quan hệ mới
            ResidentHouse rh = new ResidentHouse();
            rh.setResident(resident);
            rh.setHouseReg(house);
            rh.setIsMaster(isMaster);

            repo.addResidentHouse(rh);

            logger.info("Đã gán cư dân {} vào căn hộ {} (chủ hộ: {})",
                    resident.getResidentId(), house.getHouseId(), isMaster);

            // Ghi audit log
            AuditLogService.log(
                    "RESIDENT_HOUSE",
                    resident.getResidentId(),
                    "CREATE",
                    String.format("Gán cư dân %s vào căn hộ %s (chủ hộ: %b)",
                            resident.getResidentId(), house.getHouseId(), isMaster)
            );
            return true;

        } catch (Exception e) {
            logger.error("addResidentToHouse failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Xóa cư dân khỏi căn hộ.
     *
     * <p>Yêu cầu quyền MANAGER hoặc ADMIN.
     * Nếu người bị xóa là chủ hộ, căn hộ đó sẽ không còn chủ hộ.
     *
     * @param residentId mã cư dân
     * @param houseId    mã hộ khẩu
     * @return true nếu xóa thành công, false nếu không tìm thấy hoặc lỗi
     */
    public boolean removeResidentFromHouse(String residentId, String houseId) {
        // Kiểm tra quyền
        if (!AuthManager.hasManagerRole()) {
            logger.warn("User {} không có quyền xóa cư dân khỏi căn hộ",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }

        // Validate đầu vào
        if (residentId == null || residentId.isBlank()) {
            logger.warn("removeResidentFromHouse: residentId is blank");
            return false;
        }
        if (houseId == null || houseId.isBlank()) {
            logger.warn("removeResidentFromHouse: houseId is blank");
            return false;
        }

        try {
            ResidentHouse rh = repo.findByCompositeId(residentId, houseId);
            if (rh == null) {
                logger.warn("Không tìm thấy mối quan hệ resident={} house={}", residentId, houseId);
                return false;
            }

            repo.deleteResidentHouse(rh);

            logger.info("Đã xóa cư dân {} khỏi căn hộ {}", residentId, houseId);

            // Ghi audit log
            AuditLogService.log(
                    "RESIDENT_HOUSE",
                    residentId,
                    "DELETE",
                    String.format("Xóa cư dân %s khỏi căn hộ %s", residentId, houseId)
            );
            return true;

        } catch (Exception e) {
            logger.error("removeResidentFromHouse failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Chỉ định (hoặc hủy) chủ hộ cho một cư dân trong căn hộ.
     *
     * <p>Logic đặc biệt:
     * <ul>
     *   <li>Nếu {@code isMaster = true}: hạ chủ hộ cũ (nếu có), rồi nâng cư dân này lên.</li>
     *   <li>Nếu {@code isMaster = false}: chỉ hạ cư dân này, không ảnh hưởng người khác.</li>
     * </ul>
     *
     * @param residentId mã cư dân
     * @param houseId    mã hộ khẩu
     * @param isMaster   true = đặt làm chủ hộ, false = hạ xuống thành viên
     * @return true nếu cập nhật thành công
     */
    public boolean setMasterOfHouse(String residentId, String houseId, boolean isMaster) {
        // Kiểm tra quyền
        if (!AuthManager.hasManagerRole()) {
            logger.warn("User {} không có quyền chỉ định chủ hộ",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }

        if (residentId == null || houseId == null) {
            logger.warn("setMasterOfHouse: null input");
            return false;
        }

        try {
            ResidentHouse rh = repo.findByCompositeId(residentId, houseId);
            if (rh == null) {
                logger.warn("Không tìm thấy mối quan hệ để cập nhật chủ hộ: {} / {}", residentId, houseId);
                return false;
            }

            // Nếu đặt làm chủ hộ → hạ chủ hộ cũ trước
            if (isMaster && !rh.isMaster()) {
                demoteCurrentMaster(houseId);
            }

            rh.setIsMaster(isMaster);
            repo.updateResidentHouse(rh);

            logger.info("Cập nhật chủ hộ: cư dân={}, căn hộ={}, isMaster={}",
                    residentId, houseId, isMaster);

            // Ghi audit log
            String action = isMaster ? "Chỉ định chủ hộ" : "Hạ xuống thành viên";
            AuditLogService.log(
                    "RESIDENT_HOUSE",
                    residentId,
                    "UPDATE",
                    String.format("%s: cư dân %s tại căn hộ %s", action, residentId, houseId)
            );
            return true;

        } catch (Exception e) {
            logger.error("setMasterOfHouse failed: {}", e.getMessage(), e);
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════

    /**
     * Hạ chủ hộ hiện tại của căn hộ xuống thành viên (nếu có).
     * Được gọi nội bộ trước khi đặt chủ hộ mới — đảm bảo mỗi căn hộ
     * chỉ có tối đa 1 chủ hộ.
     *
     * @param houseId mã hộ khẩu
     */
    private void demoteCurrentMaster(String houseId) {
        try {
            ResidentHouse currentMaster = repo.findMasterByHouseId(houseId);
            if (currentMaster != null && currentMaster.isMaster()) {
                currentMaster.setIsMaster(false);
                repo.updateResidentHouse(currentMaster);
                logger.info("Đã hạ chủ hộ cũ: cư dân={} tại căn hộ={}",
                        currentMaster.getResident().getResidentId(), houseId);
            }
        } catch (Exception e) {
            // Không ném exception — thao tác chính vẫn tiếp tục
            logger.error("demoteCurrentMaster failed for houseId={}: {}", houseId, e.getMessage(), e);
        }
    }
}