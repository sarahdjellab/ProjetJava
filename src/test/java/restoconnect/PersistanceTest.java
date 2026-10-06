package restoconnect;
import org.junit.jupiter.api.*;
import org.hibernate.SessionFactory;
import restoconnect.modele.*;
import restoconnect.service.*;
import restoconnect.exceptions.*;
import restoconnect.persistance.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PersistanceTest {
    private SessionFactory fabrique;
    private Client client; private Table table; private Serveur serveur; private ArticleMenu article;
    private final LocalDateTime date = LocalDateTime.of(2035, 1, 1, 19, 0);
    private final Clock horloge = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC);
    static SessionFactory creerFabrique() {
        Properties p = new Properties(); p.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        p.setProperty("hibernate.connection.url", "jdbc:h2:mem:" + UUID.randomUUID()); p.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        return HibernateUtil.ouvrir(p);
    }
    @BeforeEach void preparer() {
        fabrique = creerFabrique();
        client = new ClientDao(fabrique).create(new Client("Dupont", "", "a@b.fr"));
        table = new TableDao(fabrique).create(new Table(1, 12));
        serveur = new ServeurDao(fabrique).create(new Serveur("Alice", "S1"));
        PlatPrincipal plat = new PlatPrincipal("Plat", 20); plat.ajouterSupplement("Sauce", 2);
        article = new ArticleMenuDao(fabrique).create(plat);
    }
    @AfterEach void fermer() { if (fabrique != null && !fabrique.isClosed()) { fabrique.close(); } }
    @Test void transactionNominaleEtRelecture() throws Exception {
        Commande c = new TransactionService(fabrique, horloge, s -> {}).reserverEtCommander(date, 4, client, table, serveur, article, 2);
        assertTrue(c.getId().isPresent());
        Commande relue = new CommandeDao(fabrique).findById(c.getId().orElseThrow()).orElseThrow();
        assertEquals(48.4, relue.calculerTotal(), .001); assertEquals(1, new ReservationDao(fabrique).findAll().size());
        assertEquals(1, relue.getLigneCommandes().size());
    }
    @Test void rollbackApresReservationDejaEcrite() {
        TransactionService service = new TransactionService(fabrique, horloge, s -> { throw new IllegalStateException("Échec simulé après flush de la réservation"); });
        PersistanceException e = assertThrows(PersistanceException.class, () -> service.reserverEtCommander(date, 4, client, table, serveur, article, 1));
        assertTrue(e.getCause().getMessage().contains("simulé"));
        assertEquals(0, new ReservationDao(fabrique).findAll().size()); assertEquals(0, new CommandeDao(fabrique).findAll().size());
    }
    @Test void conflitEnBaseEstExceptionMetier() throws Exception {
        TransactionService service = new TransactionService(fabrique, horloge, s -> {});
        service.reserverEtCommander(date, 4, client, table, serveur, article, 1);
        TableIndisponibleException e = assertThrows(TableIndisponibleException.class, () -> service.reserverEtCommander(date.plusMinutes(15), 4, client, table, serveur, article, 1));
        assertTrue(e.getMessage().contains("réservée")); assertEquals(1, new ReservationDao(fabrique).findAll().size());
    }
    @Test void crudClientEtOptional() {
        ClientDao dao = new ClientDao(fabrique); long id = client.getId().orElseThrow();
        client.setNom("Martin"); assertEquals("Martin", dao.update(client).getNom());
        assertEquals("Martin", dao.findById(id).orElseThrow().getNom()); dao.delete(id); assertTrue(dao.findById(id).isEmpty());
        dao.delete(id); assertThrows(IllegalArgumentException.class, () -> dao.findById(0));
    }
    @Test void erreurSqlEncapsuleeEtRollback() {
        PersistanceException e = assertThrows(PersistanceException.class, () -> new TableDao(fabrique).create(new Table(1, 4)));
        assertNotNull(e.getCause()); assertEquals(1, new TableDao(fabrique).findAll().size());
    }
    @Test void factureEtLignesConserveesApresRelecture() throws Exception {
        CommandeDao dao = new CommandeDao(fabrique);
        Commande c = new TransactionService(fabrique, horloge, s -> {}).reserverEtCommander(date, 10, client, table, serveur, article, 2);
        CommandeService service = new CommandeService(); service.ajouterArticle(c, article, 1);
        service.appliquerRemiseGroupe(c, 10); service.cloturerCommande(c); dao.update(c);
        Commande relue = dao.findById(c.getId().orElseThrow()).orElseThrow();
        assertTrue(relue.isCloturee()); assertEquals(65.34, relue.calculerTotal(), .001); assertEquals(2, relue.getLigneCommandes().size());
        assertThrows(IllegalStateException.class, () -> relue.ajouterLigne(article, 1));
    }
    @Test void parcoursCompletFacadeEtReprise() throws Exception {
        try (RestaurantService service = new RestaurantService(fabrique)) {
            LocalDate demain = LocalDate.now().plusDays(1);
            Client nouveau = service.creerClient("Martin", "", "martin@example.fr");
            assertEquals(2, service.listerClients().size()); assertEquals(1, service.listerServeurs().size()); assertEquals(1, service.listerArticles().size());
            assertEquals(1, service.rechercherTables(demain, "19:00", "10").size());
            LocalDateTime debut = service.preparerReservation(demain, "19:00", "10", nouveau, table);
            Commande c = service.reserverEtCommander(debut, 10, nouveau, table, serveur, article, "1");
            c = service.ajouterLigne(c, article, "1"); c = service.facturer(c);
            assertEquals("43,56 €", service.montant(c)); assertEquals(1, service.listerCommandes().size());
            assertTrue(service.rechercherTables(demain, "19:00", "2").isEmpty());
            Commande facture = c; assertThrows(IllegalStateException.class, () -> service.facturer(facture));
        }
    }
    @Test void initialisationIdempotenteDuJeuDeDemonstration() {
        fabrique.close(); fabrique = creerFabrique();
        try (RestaurantService service = new RestaurantService(fabrique)) {
            assertEquals(4, service.rechercherTables(LocalDate.now().plusDays(1), "19:30", "2").size());
            assertEquals(3, service.listerArticles().size()); assertEquals(1, service.listerClients().size());
        }
    }
}
