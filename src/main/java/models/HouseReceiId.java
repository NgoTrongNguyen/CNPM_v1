package models;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class HouseReceiId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "house_id", nullable = false)
    private HouseReg houseReg;

    @ManyToOne
    @JoinColumn(name = "recei_id", nullable = false)
    private Receivable receivable;

    public HouseReceiId() {}

    public HouseReceiId(HouseReg houseReg, Receivable receivable) {
        this.houseReg = houseReg;
        this.receivable = receivable;
    }

    public HouseReg getHouseReg() {
        return houseReg;
    }

    public void setHouseReg(HouseReg houseReg) {
        this.houseReg = houseReg;
    }

    public Receivable getReceivable() {
        return receivable;
    }

    public void setReceivable(Receivable receivable) {
        this.receivable = receivable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HouseReceiId that = (HouseReceiId) o;
        return Objects.equals(houseReg != null ? houseReg.getHouseId() : null,
                that.houseReg != null ? that.houseReg.getHouseId() : null) &&
                Objects.equals(receivable != null ? receivable.getReceiId() : null,
                        that.receivable != null ? that.receivable.getReceiId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(houseReg != null ? houseReg.getHouseId() : null,
                receivable != null ? receivable.getReceiId() : null);
    }
}