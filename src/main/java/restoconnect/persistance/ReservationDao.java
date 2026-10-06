package restoconnect.persistance;
import org.hibernate.SessionFactory;
import restoconnect.modele.Reservation;

public class ReservationDao extends Dao<Reservation> {
    public ReservationDao(SessionFactory fabrique) { super(fabrique, Reservation.class); }
}
