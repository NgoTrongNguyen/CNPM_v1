package models;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "house_recei")
public class HouseRecei {

    @EmbeddedId
    private HouseReceiId id;

    @Column(name = "status", nullable = false)
    private boolean status;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    @Column(name = "pay_date")
    private Instant payDate;

    @Column(name = "pay_deadline", nullable = false)
    private Instant payDeadline;

    @Column(name = "description")
    private String description;

    public HouseRecei() {}

    public HouseReceiId getId() {return id;}
    public void setId(HouseReceiId id) {this.id = id;}

    public HouseReg getHouseReg() {
        return id != null ? id.getHouseReg() : null;
    }
    public void setHouseReg(HouseReg houseReg) {
        if (id == null) id = new HouseReceiId();
        id.setHouseReg(houseReg);
    }

    public Receivable getReceivable() {
        return id != null ? id.getReceivable() : null;
    }
    public void setReceivable(Receivable receivable) {
        if (id == null) id = new HouseReceiId();
        id.setReceivable(receivable);
    }

    public boolean isStatus() {return status;}
    public void setStatus(boolean status) {this.status = status;}

    public long getQuantity() {return quantity;}
    public void setQuantity(long quantity) {this.quantity = quantity;}

    public Instant getPayDate() {return payDate;}
    public void setPayDate(Instant payDate) {this.payDate = payDate;}

    public Instant getPayDeadline() {return payDeadline;}
    public void setPayDeadline(Instant payDeadline) {this.payDeadline = payDeadline;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HouseRecei that = (HouseRecei) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}