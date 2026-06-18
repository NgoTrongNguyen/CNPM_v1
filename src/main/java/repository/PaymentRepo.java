package repository;

import database.DB_manager;
import models.HouseRecei;
import models.Payment;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.Instant;
import java.util.List;

/**
 * Repository truy cập bảng payment (lịch sử thanh toán).
 */
public class PaymentRepo {

    /**
     * Thêm một lần thanh toán mới.
     * Đầu vào: payment — entity Payment
     * Đầu ra: không có (ném exception nếu thất bại)
     */
    public void addPayment(Payment payment) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(payment);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Lấy lịch sử thanh toán của một khoản thu.
     */
    public List<Payment> findByHouseRecei(HouseRecei houseRecei) {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM Payment WHERE houseRecei = :hr ORDER BY paymentDate DESC",
                    Payment.class)
                    .setParameter("hr", houseRecei)
                    .list();
        }
    }

    /**
     * Lấy thanh toán trong khoảng thời gian.
     */
    public List<Payment> findByDateRange(Instant from, Instant to) {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM Payment WHERE paymentDate BETWEEN :from AND :to ORDER BY paymentDate DESC",
                    Payment.class)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .list();
        }
    }

    /**
     * Lấy tất cả thanh toán.
     */
    public List<Payment> findAll() {
        try (Session session = DB_manager.getFactory().openSession()) {
            return session.createQuery(
                    "FROM Payment ORDER BY paymentDate DESC",
                    Payment.class)
                    .list();
        }
    }

    /**
     * Tổng số tiền đã thu được.
     */
    public long getTotalPaymentAmount() {
        try (Session session = DB_manager.getFactory().openSession()) {
            Long result = session.createQuery(
                    "SELECT SUM(amountPaid) FROM Payment",
                    Long.class)
                    .uniqueResult();
            return result != null ? result : 0L;
        }
    }
}
