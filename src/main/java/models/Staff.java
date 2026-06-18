package models;

import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;

@Entity
@Table(name = "staff")
public class Staff {
    @Id
    @Column(name ="staff_id", unique = true, nullable = false, length = 20)
    private String staffId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "delete_at")
    private Instant deleteAt;

    public Staff() {}

    public String getStaffId() {return staffId;}
    public void setStaffId(String staffId) {this.staffId = staffId;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}

    public Instant getCreatedAt() {return createdAt;}
    public Instant getDeleteAt() {return deleteAt;}
    public void setDeleteAt(Instant deleteAt) {this.deleteAt = deleteAt;}

}
