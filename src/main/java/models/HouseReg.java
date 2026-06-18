package models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;

@Entity
@Table(name = "house_reg")
public class HouseReg {
    @Id
    @Column(name = "house_id", unique = true, nullable = false, length = 20)
    private String houseId;

    @ManyToOne
    @JoinColumn(name = "apart_id", nullable = false)
    private Apartment apartment;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at",insertable = false, updatable = false)
    private Instant createAt;

    @Column(name = "delete_at")
    private Instant deleteAt;

    public HouseReg() {}

    public String getHouseId() {return houseId;}
    public void setHouseId(String houseId) {this.houseId = houseId;}

    public Apartment getApartment() {return apartment;}
    public void setApartment(Apartment apartment) {this.apartment = apartment;}

    public Instant getCreateAt() {return createAt;}
    public Instant getDeleteAt() {return deleteAt;}
    public void setCreateAt(Instant createAt) {this.createAt = createAt;}

    public void setDeleteAt(Instant now) {

    }
}
