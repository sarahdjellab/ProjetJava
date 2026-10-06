package restoconnect.service;
import restoconnect.modele.*;
import restoconnect.exceptions.TableIndisponibleException;
import java.time.*;
import java.util.*;
import java.util.logging.*;

public class ReservationService {
    private static final Logger JOURNAL = Logger.getLogger(ReservationService.class.getName());
    private final Clock horloge;
    private final List<Reservation> reservations = new ArrayList<>();

    public ReservationService() { this(Clock.systemDefaultZone()); }

    public ReservationService(Clock horloge) { this.horloge = Validation.requis(horloge, "Horloge"); }

    public synchronized void charger(List<Reservation> reservations) {
        Validation.requis(reservations, "Réservations");
        reservations.forEach(r -> Validation.requis(r, "Réservation"));
        this.reservations.clear();
        this.reservations.addAll(reservations);
    }

    public synchronized Reservation reserverTable(LocalDateTime date, int personnes, Client client, Table table)
            throws TableIndisponibleException {
        validerDemande(date, personnes, client, table);
        try {
            if (!estDisponible(table, date, personnes)) {
                throw new TableIndisponibleException("La table " + table.getNumero() + " est déjà réservée sur ce créneau.");
            }
            Reservation reservation = new Reservation(date, personnes, client, table);
            reservations.add(reservation);
            return reservation;
        } catch (TableIndisponibleException erreur) {
            JOURNAL.log(Level.WARNING, erreur.getMessage(), erreur);
            throw erreur;
        } finally {
            JOURNAL.info("Demande de réservation traitée pour la table " + table.getNumero() + ".");
        }
    }

    public void validerDemande(LocalDateTime date, int personnes, Client client, Table table) {
        Validation.requis(date, "Date");
        Validation.positif(personnes);
        Validation.requis(client, "Client");
        Validation.requis(table, "Table");
        if (date.isBefore(LocalDateTime.now(horloge))) { throw new IllegalArgumentException("La réservation ne peut pas être dans le passé."); }
        if (personnes > table.getCapacite()) { throw new IllegalArgumentException("Le nombre de personnes dépasse la capacité de la table."); }
    }

    public synchronized boolean estDisponible(Table table, LocalDateTime date, int personnes) {
        Validation.requis(table, "Table");
        Validation.requis(date, "Date");
        Validation.positif(personnes);
        if (date.isBefore(LocalDateTime.now(horloge))) { throw new IllegalArgumentException("La date est passée."); }
        if (personnes > table.getCapacite()) { return false; }
        return reservations.stream().noneMatch(r -> r.getTable().getNumero() == table.getNumero()
                && date.isBefore(r.getFin()) && r.getDateHeure().isBefore(date.plusHours(2)));
    }

    public List<Table> rechercherTables(List<Table> tables, LocalDateTime date, int personnes) {
        Validation.requis(tables, "Tables");
        Validation.requis(date, "Date");
        Validation.positif(personnes);
        if (date.isBefore(LocalDateTime.now(horloge))) { throw new IllegalArgumentException("La date est passée."); }
        tables.forEach(t -> Validation.requis(t, "Table"));
        return tables.stream().filter(t -> estDisponible(t, date, personnes)).toList();
    }

    public synchronized List<Reservation> lister() { return List.copyOf(reservations); }
}
