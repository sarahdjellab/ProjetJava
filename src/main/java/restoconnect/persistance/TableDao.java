package restoconnect.persistance;
import org.hibernate.SessionFactory;
import restoconnect.modele.Table;

public class TableDao extends Dao<Table> {
    public TableDao(SessionFactory fabrique) { super(fabrique, Table.class); }
}
