package restoconnect.persistance;
import org.hibernate.SessionFactory;
import restoconnect.modele.Serveur;

public class ServeurDao extends Dao<Serveur> {
    public ServeurDao(SessionFactory fabrique) { super(fabrique, Serveur.class); }
}
