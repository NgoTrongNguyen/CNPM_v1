package models;

import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "staff_detail")
public class StaffDetail {
    @OneToOne
    @Id
    @JoinColumn(name = "staff_id", referencedColumnName = "staff_id", unique = true)
    private Staff staff;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @Column(name = "address")
    private String address;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public StaffDetail(){}

    public Staff getStaff() {return staff;}
    public void setStaff(Staff staff) {this.staff = staff;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public LocalDate getBirthday() {return birthday;}
    public void setBirthday(LocalDate birthday) {this.birthday = birthday;}

    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;}

    public Instant getCreatedAt() {return createdAt;}
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaffDetail that = (StaffDetail) o;
        return Objects.equals(staff != null ? staff.getStaffId() : null,
                that.staff != null ? that.staff.getStaffId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(staff != null ? staff.getStaffId() : null);
    }
}
