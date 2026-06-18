package models;

import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;

@Entity
@Table(name = "receivable")
public class Receivable {
    @Id
    @Column(name = "recei_id", nullable = false, unique = true)
    private String receiId;

    @Column(name = "recei_name", nullable = false)
    private String receiName;

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory;

    @Column(name = "fixed", nullable = false)
    private boolean fixed;

    @Column(name ="price", nullable = false)
    private long price;

    @Column(name = "description")
    private String description;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "delete_at")
    private Instant deleteAt;

    public Receivable() {}

    public String getReceiId() {return receiId;}
    public void setReceiId(String receiId) {this.receiId = receiId;}

    public String getReceiName() {return receiName;}
    public void setReceiName(String receiName) {this.receiName = receiName;}

    public boolean isMandatory() {return mandatory;}
    public void setMandatory(boolean mandatory) {this.mandatory = mandatory;}

    public boolean isFixed() {return fixed;}
    public void setFixed(boolean fixed) {this.fixed = fixed;}

    public long getPrice() {return price;}
    public void setPrice(long price) {this.price = price;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public Instant getCreatedAt() {return createdAt;}
    public Instant getDeleteAt() {return deleteAt;}
    public void setDeleteAt(Instant deleteAt) {this.deleteAt = deleteAt;}
}

