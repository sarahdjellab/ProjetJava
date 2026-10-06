package restoconnect.persistance;
import org.hibernate.SessionFactory;
import restoconnect.modele.Commande;

public class CommandeDao extends Dao<Commande> {
    public CommandeDao(SessionFactory fabrique) { super(fabrique, Commande.class); }
}
