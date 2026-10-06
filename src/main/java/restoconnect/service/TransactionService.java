package restoconnect.service;
import org.hibernate.*;
import jakarta.persistence.LockModeType;
import restoconnect.modele.*;
import restoconnect.exceptions.*;
import restoconnect.persistance.ValidationEntite;
import java.time.*;
import java.util.function.Consumer;
import java.util.logging.*;

public class TransactionService {
    private static final Logger JOURNAL = Logger.getLogger(TransactionService.class.getName());
    private final SessionFactory fabrique;
    private final Clock horloge;
    private final Consumer<Session> apresReservation;

    public TransactionService(SessionFactory fabrique) { this(fabrique, Clock.systemDefaultZone(), s -> { }); }

    public TransactionService(SessionFactory fabrique, Clock horloge, Consumer<Session> apresReservation) {
        this.fabrique = Validation.requis(fabrique, "Fabrique");
        this.horloge = Validation.requis(horloge, "Horloge");
        this.apresReservation = Validation.requis(apresReservation, "Action");
    }

    public Commande reserverEtCommander(LocalDateTime date, int personnes, Client client, Table table,
            Serveur serveur, ArticleMenu article, int quantite) throws TableIndisponibleException {
        new ReservationService(horloge).validerDemande(date, personnes, client, table);
        Validation.requis(serveur, "Serveur"); Validation.requis(article, "Article"); Validation.positif(quantite);
        long tableId = table.getId().orElseThrow(() -> new IllegalArgumentException("La table doit être sauvegardée."));
        long clientId = client.getId().orElseThrow(() -> new IllegalArgumentException("Le client doit être sauvegardé."));
        long serveurId = serveur.getId().orElseThrow(() -> new IllegalArgumentException("Le serveur doit être sauvegardé."));
        long articleId = article.getId().orElseThrow(() -> new IllegalArgumentException("L'article doit être sauvegardé."));
        try (Session session = fabrique.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Table tableGeree = session.find(Table.class, tableId, LockModeType.PESSIMISTIC_WRITE);
                Client clientGere = session.get(Client.class, clientId);
                Serveur serveurGere = session.get(Serveur.class, serveurId);
                ArticleMenu articleGere = session.get(ArticleMenu.class, articleId);
                new ReservationService(horloge).validerDemande(date, personnes, clientGere, tableGeree);
                Validation.requis(serveurGere, "Serveur en base"); Validation.requis(articleGere, "Article en base");
                long conflits = session.createQuery("select count(r) from Reservation r where r.table.id = :table and r.dateHeure < :fin and r.dateHeure > :debutMoinsDeux", Long.class)
                        .setParameter("table", tableId).setParameter("fin", date.plusHours(2)).setParameter("debutMoinsDeux", date.minusHours(2)).getSingleResult();
                if (conflits > 0) { throw new TableIndisponibleException("Cette table est déjà réservée sur ce créneau."); }
                Reservation reservation = new Reservation(date, personnes, clientGere, tableGeree);
                ValidationEntite.verifier(reservation);
                session.persist(reservation);
                session.flush();
                apresReservation.accept(session);
                Commande commande = new CommandeService().creerCommande(reservation, serveurGere, articleGere, quantite);
                ValidationEntite.verifier(commande);
                session.persist(commande);
                transaction.commit();
                return commande;
            } catch (TableIndisponibleException erreur) {
                if (transaction.isActive()) { transaction.rollback(); }
                JOURNAL.log(Level.WARNING, erreur.getMessage(), erreur); throw erreur;
            } catch (RuntimeException erreur) {
                if (transaction.isActive()) { transaction.rollback(); }
                JOURNAL.log(Level.SEVERE, "Transaction réservation et commande annulée.", erreur);
                throw new PersistanceException("La réservation et la commande ont été annulées.", erreur);
            } finally { JOURNAL.info("Tentative de réservation et commande terminée."); }
        } catch (PersistanceException erreur) {
            JOURNAL.log(Level.SEVERE, erreur.getMessage(), erreur); throw erreur;
        } catch (RuntimeException erreur) {
            JOURNAL.log(Level.SEVERE, "Base indisponible.", erreur);
            throw new PersistanceException("La base de données est indisponible.", erreur);
        }
    }
}
