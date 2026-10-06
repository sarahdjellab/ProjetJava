package restoconnect.persistance;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import restoconnect.modele.*;
import restoconnect.exceptions.PersistanceException;
import java.nio.file.*;
import java.io.*;
import java.util.Properties;
import java.util.logging.*;

public final class HibernateUtil {
    private static final Logger JOURNAL = Logger.getLogger(HibernateUtil.class.getName());
    private HibernateUtil() { }

    public static SessionFactory ouvrir() {
        Properties proprietes = new Properties();
        Path chemin = Path.of("config/database.properties");
        if (Files.exists(chemin)) {
            try (InputStream flux = Files.newInputStream(chemin)) { proprietes.load(flux); }
            catch (IOException erreur) {
                JOURNAL.log(Level.SEVERE, "Lecture de configuration impossible.", erreur);
                throw new PersistanceException("La configuration de la base est illisible.", erreur);
            }
        }
        proprietes.putIfAbsent("hibernate.connection.driver_class", "org.h2.Driver");
        proprietes.putIfAbsent("hibernate.connection.url", "jdbc:h2:file:./data/restoconnect");
        proprietes.putIfAbsent("hibernate.hbm2ddl.auto", "update");
        return ouvrir(proprietes);
    }

    public static SessionFactory ouvrir(Properties proprietes) {
        restoconnect.service.Validation.requis(proprietes, "Configuration");
        try {
            Configuration configuration = new Configuration().addProperties(proprietes);
            for (Class<?> classe : new Class<?>[]{Client.class, Serveur.class, Table.class,
                    Reservation.class, ArticleMenu.class, Entree.class, PlatPrincipal.class, Dessert.class, Commande.class}) {
                configuration.addAnnotatedClass(classe);
            }
            return configuration.buildSessionFactory();
        } catch (RuntimeException erreur) {
            JOURNAL.log(Level.SEVERE, "Ouverture de la base impossible.", erreur);
            throw new PersistanceException("Impossible d'ouvrir la base de données.", erreur);
        }
    }
}
