package models;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "resident_house")
public class ResidentHouse {
    @ManyToOne
    @Id
    @JoinColumn(name = "resident_id", unique = true, nullable = false)
    private Resident resident;

    @ManyToOne
    @JoinColumn(name = "house_id", nullable = false)
    private HouseReg houseReg;

    @Column(name = "isMaster", nullable = false)
    private boolean isMaster;

    public ResidentHouse() {}

    public Resident getResident() {return resident;}
    public void setResident(Resident resident) {this.resident = resident;}

    public HouseReg getHouseReg() {return houseReg;}
    public void setHouseReg(HouseReg houseReg) {this.houseReg = houseReg;}

    public boolean isMaster() {return isMaster;}
    public void setIsMaster(boolean isMaster) {this.isMaster = isMaster;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResidentHouse that = (ResidentHouse) o;

        // So sánh ID của các object thay vì so sánh cả object
        return Objects.equals(resident != null ? resident.getResidentId() : null,
                that.resident != null ? that.resident.getResidentId() : null) &&
                Objects.equals(houseReg != null ? houseReg.getHouseId() : null,
                        that.houseReg != null ? that.houseReg.getHouseId() : null);
    }

    @Override
    public int hashCode() {
        // Chỉ hash dựa trên ID của đối tượng
        return Objects.hash(resident != null ? resident.getResidentId() : null,
                houseReg != null ? houseReg.getHouseId() : null);
    }
}
