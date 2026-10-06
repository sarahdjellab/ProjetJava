package restoconnect.service;
import restoconnect.modele.*;
import restoconnect.exceptions.*;
import restoconnect.persistance.*;
import org.hibernate.SessionFactory;
import java.time.*;
import java.util.*;

public class RestaurantService implements AutoCloseable {
    private final SessionFactory fabrique;
    private final ClientDao clients;
    private final ServeurDao serveurs;
    private final TableDao tables;
    private final ArticleMenuDao articles;
    private final CommandeDao commandes;
    private final ReservationDao reservations;
    private final CommandeService commandeService = new CommandeService();
    private final ReservationService reservationService = new ReservationService();

    public RestaurantService() { this(HibernateUtil.ouvrir()); }

    public RestaurantService(SessionFactory fabrique) {
        this.fabrique = Validation.requis(fabrique, "Fabrique");
        clients = new ClientDao(fabrique); serveurs = new ServeurDao(fabrique); tables = new TableDao(fabrique);
        articles = new ArticleMenuDao(fabrique); commandes = new CommandeDao(fabrique); reservations = new ReservationDao(fabrique);
        if (tables.findAll().isEmpty()) { tables.create(new Table(1, 2)); tables.create(new Table(2, 4)); tables.create(new Table(3, 6)); tables.create(new Table(4, 12)); }
        if (clients.findAll().isEmpty()) { clients.create(new Client("Camille Dupont", "0600000000", "camille@example.fr")); }
        if (serveurs.findAll().isEmpty()) { serveurs.create(new Serveur("Alice", "S001")); }
        if (articles.findAll().isEmpty()) { articles.create(new Entree("Salade de saison", 8)); articles.create(new PlatPrincipal("Poulet rôti", 16)); articles.create(new Dessert("Tarte aux pommes", 7)); }
    }

    public List<Client> listerClients() { return clients.findAll(); }

    public List<Serveur> listerServeurs() { return serveurs.findAll(); }

    public List<ArticleMenu> listerArticles() { return articles.findAll(); }

    public List<Commande> listerCommandes() { return commandes.findAll(); }

    public Client creerClient(String nom, String telephone, String email) { return clients.create(new Client(nom, telephone, email)); }

    public List<Table> rechercherTables(LocalDate date, String heure, String effectif) {
        LocalDateTime debut = Validation.dateHeure(date, heure);
        int personnes = Validation.entier(effectif);
        reservationService.charger(reservations.findAll());
        return reservationService.rechercherTables(tables.findAll(), debut, personnes);
    }

    public LocalDateTime preparerReservation(LocalDate date, String heure, String effectif, Client client, Table table) {
        LocalDateTime debut = Validation.dateHeure(date, heure);
        reservationService.validerDemande(debut, Validation.entier(effectif), client, table);
        return debut;
    }

    public Commande reserverEtCommander(LocalDateTime debut, int effectif, Client client, Table table, Serveur serveur, ArticleMenu article, String quantite)
            throws TableIndisponibleException {
        Validation.requis(debut, "Début"); Validation.positif(effectif); Validation.requis(client, "Client"); Validation.requis(table, "Table");
        Validation.requis(serveur, "Serveur"); Validation.requis(article, "Article"); int nombre = Validation.entier(quantite);
        return new TransactionService(fabrique).reserverEtCommander(debut, effectif, client, table, serveur, article, nombre);
    }

    public Commande ajouterLigne(Commande commande, ArticleMenu article, String quantite) {
        Validation.requis(commande, "Commande"); Validation.requis(article, "Article"); int nombre = Validation.entier(quantite);
        Commande copie = retrouver(commande);
        commandeService.ajouterArticle(copie, article, nombre);
        return commandes.update(copie);
    }

    public Commande facturer(Commande commande) {
        Validation.requis(commande, "Commande");
        Commande copie = retrouver(commande);
        commandeService.appliquerRemiseGroupe(copie, copie.getReservation().getNombrePersonnes());
        commandeService.cloturerCommande(copie);
        return commandes.update(copie);
    }

    public String montant(Commande commande) { return String.format(Locale.FRANCE, "%.2f €", commandeService.calculerMontantCommande(commande)); }
    private Commande retrouver(Commande commande) {
        long id = commande.getId().orElseThrow(() -> new IllegalArgumentException("Commande non sauvegardée."));
        return commandes.findById(id).orElseThrow(() -> new IllegalArgumentException("Commande introuvable."));
    }

    @Override public void close() { fabrique.close(); }
}
