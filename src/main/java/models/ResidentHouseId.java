package models;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ResidentHouseId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @ManyToOne
    @JoinColumn(name = "house_id", nullable = false)
    private HouseReg houseReg;

    public ResidentHouseId() {}

    public ResidentHouseId(Resident resident, HouseReg houseReg) {
        this.resident = resident;
        this.houseReg = houseReg;
    }

    public Resident getResident() {
        return resident;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
    }

    public HouseReg getHouseReg() {
        return houseReg;
    }

    public void setHouseReg(HouseReg houseReg) {
        this.houseReg = houseReg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResidentHouseId that = (ResidentHouseId) o;
        return Objects.equals(resident != null ? resident.getResidentId() : null,
                that.resident != null ? that.resident.getResidentId() : null) &&
                Objects.equals(houseReg != null ? houseReg.getHouseId() : null,
                        that.houseReg != null ? that.houseReg.getHouseId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resident != null ? resident.getResidentId() : null,
                houseReg != null ? houseReg.getHouseId() : null);
    }
}