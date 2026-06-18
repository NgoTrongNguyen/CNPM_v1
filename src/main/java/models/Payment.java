package models;

import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;

/**
 * Một lần thanh toán cho một khoản thu của căn hộ (HouseRecei).
 * Cho phép theo dõi lịch sử thanh toán chi tiết, phương thức, giao dịch.
 */
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @Column(name = "payment_id", nullable = false, length = 20)
    private String paymentId;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "house_id", referencedColumnName = "house_id"),
            @JoinColumn(name = "recei_id", referencedColumnName = "recei_id")
    })
    private HouseRecei houseRecei;

    @Column(name = "amount_paid", nullable = false)
    private long amountPaid;            // số tiền đã thanh toán

    @Column(name = "payment_date", nullable = false)
    private Instant paymentDate;

    @Column(name = "payment_method")    // CASH, BANK_TRANSFER, CHECK
    private String paymentMethod;

    @Column(name = "transaction_id")    // mã giao dịch từ ngân hàng
    private String transactionId;

    @Column(name = "notes")
    private String notes;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Payment() {}

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public HouseRecei getHouseRecei() { return houseRecei; }
    public void setHouseRecei(HouseRecei houseRecei) { this.houseRecei = houseRecei; }

    public long getAmountPaid() { return amountPaid; }
    public void setAmountPaid(long amountPaid) { this.amountPaid = amountPaid; }

    public Instant getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Instant paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
}
