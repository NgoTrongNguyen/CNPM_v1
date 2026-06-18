package models;

import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "resident")
public class Resident {
    @Id
    @Column(name ="resident_id", unique = true, nullable = false, length = 20)
    private String residentId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "telephone", unique = true, length = 20)
    private String telephone;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name ="delete_at")
    private Instant deleteAt;

    public Resident() {}

    public String getResidentId() {return residentId;}
    public void setResidentId(String residentId) { this.residentId = residentId; }

    public String getName() {return name;}
    public void setName(String name) { this.name = name; }

    public LocalDate getBirthday() {return birthday;}
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    public String getTelephone() {return telephone;}
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public Instant getCreatedAt() {return createdAt;}
    public Instant getDeleteAt() {return deleteAt;}
    public void setDeleteAt(Instant deleteAt) { this.deleteAt = deleteAt; }
}
