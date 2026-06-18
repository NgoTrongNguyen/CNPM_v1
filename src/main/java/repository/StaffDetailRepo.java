package repository;

import database.DB_manager;
import models.Staff;
import models.StaffDetail;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class StaffDetailRepo {

    public StaffDetail findByStaffId(String staffId) {
        try (Session session = DB_manager.getFactory().openSession()) {
            // Dùng HQL truy vấn qua khóa ngoại staff.id thay vì session.get(StaffDetail.class, staff)
            // vì @OneToOne @Id @JoinColumn không tự ánh xạ đúng id type cho session.get khi không dùng @MapsId
            return session.createQuery(
                            "FROM StaffDetail sd WHERE sd.staff.id = :staffId",
                            StaffDetail.class)
                    .setParameter("staffId", staffId)
                    .uniqueResultOptional()
                    .orElse(null);
        }
    }

    public void updateDetail(StaffDetail detail) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(detail);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void changePassword(Staff staff, String newPassword) {
        Transaction tx = null;
        try (Session session = DB_manager.getFactory().openSession()) {
            tx = session.beginTransaction();
            Staff managed = session.merge(staff);
            managed.setPassword(newPassword);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
