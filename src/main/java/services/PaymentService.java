package services;

import models.HouseRecei;
import models.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.PaymentRepo;
import user.AuthManager;

import java.time.Instant;
import java.util.List;

/**
 * Service quản lý thanh toán (Payment).
 * Kiểm tra quyền (FINANCE/ADMIN), ghi nhật ký audit, tổng hợp số liệu.
 */
public class PaymentService {

    private final PaymentRepo repo;
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    public PaymentService(PaymentRepo repo) {
        this.repo = repo;
    }

    /**
     * Ghi nhận một lần thanh toán mới — chỉ FINANCE/ADMIN.
     * Đầu vào: payment — entity Payment hợp lệ
     * Đầu ra: true nếu thành công, false nếu thất bại / không đủ quyền
     */
    public boolean recordPayment(Payment payment) {
        if (!AuthManager.hasFinanceRole()) {
            logger.warn("User {} không có quyền ghi nhận thanh toán",
                    AuthManager.getCurrentUser().getUserId());
            return false;
        }

        if (payment == null || payment.getPaymentId() == null || payment.getPaymentId().isBlank()) {
            logger.warn("Cannot record payment: invalid data");
            return false;
        }

        try {
            if (payment.getPaymentDate() == null) {
                payment.setPaymentDate(Instant.now());
            }
            repo.addPayment(payment);
            logger.info("Recorded payment: {} - Amount: {}",
                    payment.getPaymentId(), payment.getAmountPaid());

            AuditLogService.log("PAYMENT", payment.getPaymentId(), "CREATE",
                    String.format("Payment recorded: %s, Amount: %d",
                            payment.getPaymentMethod(), payment.getAmountPaid()));
            return true;
        } catch (Exception e) {
            logger.error("recordPayment failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lấy lịch sử thanh toán của một khoản thu.
     */
    public List<Payment> getPaymentHistory(HouseRecei houseRecei) {
        try {
            return repo.findByHouseRecei(houseRecei);
        } catch (Exception e) {
            logger.error("getPaymentHistory failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Lấy tất cả thanh toán.
     */
    public List<Payment> findAll() {
        try {
            return repo.findAll();
        } catch (Exception e) {
            logger.error("findAll payments failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Tổng tiền đã thu.
     */
    public long getTotalCollected() {
        try {
            return repo.getTotalPaymentAmount();
        } catch (Exception e) {
            logger.error("getTotalCollected failed: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Lấy thanh toán trong khoảng thời gian.
     */
    public List<Payment> getPaymentsByDate(Instant from, Instant to) {
        try {
            return repo.findByDateRange(from, to);
        } catch (Exception e) {
            logger.error("getPaymentsByDate failed: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
