package restoconnect.persistance;
import org.hibernate.*;
import restoconnect.service.Validation;
import restoconnect.exceptions.PersistanceException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.*;
 public class Dao<T> {
    private static final Logger JOURNAL = Logger.getLogger(Dao.class.getName());
    private final SessionFactory fabrique;
    private final Class<T> type;

    public Dao(SessionFactory fabrique, Class<T> type) {
        this.fabrique = Validation.requis(fabrique, "Fabrique");
        this.type = Validation.requis(type, "Type");
    }

    public T create(T entite) {
        ValidationEntite.verifier(entite);
        return ecrire(s -> { s.persist(entite); return entite; });
    }

    public Optional<T> findById(long id) {
        if (id <= 0) { throw new IllegalArgumentException("Identifiant invalide."); }
        return lire(s -> Optional.ofNullable(s.get(type, id)));
    }

    public List<T> findAll() { return lire(s -> s.createQuery("from " + type.getSimpleName(), type).getResultList()); }

    public T update(T entite) {
        ValidationEntite.verifier(entite);
        return ecrire(s -> s.merge(entite));
    }

    public void delete(long id) {
        if (id <= 0) { throw new IllegalArgumentException("Identifiant invalide."); }
        ecrire(s -> { T entite = s.get(type, id); if (entite != null) { s.remove(entite); } return Boolean.TRUE; });
    }
    private <R> R lire(Function<Session, R> action) {
        try (Session session = fabrique.openSession()) { return action.apply(session); }
        catch (RuntimeException erreur) {
            JOURNAL.log(Level.SEVERE, "Lecture en base impossible.", erreur);
            throw new PersistanceException("La lecture en base a échoué.", erreur);
        }
    }
    private <R> R ecrire(Function<Session, R> action) {
        try (Session session = fabrique.openSession()) {
            Transaction transaction = session.beginTransaction();
            try { R resultat = action.apply(session); transaction.commit(); return resultat; }
            catch (RuntimeException erreur) {
                if (transaction.isActive()) { transaction.rollback(); }
                JOURNAL.log(Level.SEVERE, "Écriture annulée.", erreur);
                throw new PersistanceException("La sauvegarde a échoué ; aucune modification conservée.", erreur);
            }
        } catch (PersistanceException erreur) {
            JOURNAL.log(Level.SEVERE, erreur.getMessage(), erreur); throw erreur;
        } catch (RuntimeException erreur) {
            JOURNAL.log(Level.SEVERE, "Session indisponible.", erreur);
            throw new PersistanceException("La base est indisponible.", erreur);
        }
    }
}
