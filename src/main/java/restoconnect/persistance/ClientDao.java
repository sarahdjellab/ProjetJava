package restoconnect.persistance;
import org.hibernate.SessionFactory;
import restoconnect.modele.Client;

public class ClientDao extends Dao<Client> {
    public ClientDao(SessionFactory fabrique) { super(fabrique, Client.class); }
}
