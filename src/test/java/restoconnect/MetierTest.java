package restoconnect;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import restoconnect.modele.*;
import restoconnect.service.*;
import restoconnect.exceptions.*;
import java.time.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MetierTest {
    private Client client;
    private Table table;
    private Serveur serveur;
    private ReservationService reservations;
    private CommandeService commandes;
    private LocalDateTime date;
    @TempDir Path dossier;
    @BeforeEach void preparer() {
        client = new Client("Dupont", "0600000000", "dupont@example.fr");
        table = new Table(1, 12); serveur = new Serveur("Alice", "S1");
        reservations = new ReservationService(Clock.fixed(Instant.parse("2030-01-01T10:00:00Z"), ZoneOffset.UTC));
        commandes = new CommandeService(); date = LocalDateTime.of(2030, 1, 1, 19, 0);
    }
    private Commande commande(int personnes) { return new Commande(new Reservation(date, personnes, client, table), serveur, new PlatPrincipal("Plat", 20), 2); }
    @Test void reservationNominale() throws Exception {
        Reservation r = reservations.reserverTable(date, 4, client, table);
        assertEquals(date.plusHours(2), r.getFin()); assertEquals(1, reservations.lister().size());
        assertSame(client, r.getClient()); assertSame(table, r.getTable());
    }
    @Test void chevauchementRefuseEtSansAjout() throws Exception {
        reservations.reserverTable(date, 4, client, table);
        TableIndisponibleException e = assertThrows(TableIndisponibleException.class, () -> reservations.reserverTable(date.plusMinutes(30), 4, client, table));
        assertTrue(e.getMessage().contains("table 1")); assertEquals(1, reservations.lister().size());
    }
    @Test void chevauchementAvantLeDebutRefuse() throws Exception {
        reservations.reserverTable(date, 4, client, table);
        assertThrows(TableIndisponibleException.class, () -> reservations.reserverTable(date.minusMinutes(30), 4, client, table));
    }
    @Test void creneauxAdjacentsAcceptes() throws Exception {
        reservations.reserverTable(date, 4, client, table);
        assertDoesNotThrow(() -> reservations.reserverTable(date.plusHours(2), 4, client, table));
    }
    @Test void depassementCapaciteRefuse() { assertThrows(IllegalArgumentException.class, () -> reservations.reserverTable(date, 13, client, table)); }
    @Test void reservationDansLePasseRefusee() { assertThrows(IllegalArgumentException.class, () -> reservations.reserverTable(date.minusDays(1), 4, client, table)); }
    @Test void commandeSansLigneImpossible() {
        assertThrows(IllegalArgumentException.class, () -> commandes.creerCommande(new Reservation(date, 2, client, table), serveur, null, 1));
        assertThrows(IllegalArgumentException.class, () -> commandes.creerCommande(new Reservation(date, 2, client, table), serveur, new Entree("Salade"), 0));
    }
    @Test void doubleClotureRefusee() {
        Commande c = commande(2); assertEquals(44, commandes.cloturerCommande(c), .001);
        assertThrows(IllegalStateException.class, () -> commandes.cloturerCommande(c)); assertTrue(c.isCloturee());
    }
    @Test void ajoutApresClotureRefuse() {
        Commande c = commande(2); commandes.cloturerCommande(c);
        assertThrows(IllegalStateException.class, () -> commandes.ajouterArticle(c, new Dessert("Tarte"), 1));
    }
    @Test void troisCalculsPolymorphes() {
        List<ArticleMenu> articles = List.of(new Entree("Salade", 10), new PlatPrincipal("Plat", 10), new Dessert("Tarte", 10));
        assertArrayEquals(new double[]{10.5, 11, 12}, articles.stream().mapToDouble(commandes::calculerPrixArticle).toArray(), .001);
    }
    @Test void supplementsEtEncapsulation() {
        Entree entree = new Entree("Salade"); entree.ajouterSupplement("Fromage", 2);
        PlatPrincipal plat = new PlatPrincipal("Plat"); plat.ajouterSupplement("Sauce", 3);
        assertEquals(12.6, entree.calculerPrixTTC(), .001); assertEquals(14.3, plat.calculerPrixTTC(), .001);
        assertThrows(UnsupportedOperationException.class, () -> entree.listerSupplements().put("Intrusion", 1.0));
        assertThrows(IllegalArgumentException.class, () -> plat.ajouterSupplement("", 1));
        assertThrows(IllegalArgumentException.class, () -> entree.ajouterSupplement("Sel", Double.NaN));
    }
    @Test void prixFigéLorsDeLAjout() {
        Entree e = new Entree("Salade", 10);
        Commande c = new Commande(new Reservation(date, 2, client, table), serveur, e, 1);
        e.setPrixBase(20); e.setNom("Autre salade");
        assertEquals(10.5, commandes.calculerMontantCommande(c), .001);
        assertThrows(UnsupportedOperationException.class, () -> c.getLigneCommandes().clear());
        assertEquals(1, c.getLigneCommandes().getFirst().getQuantite());
    }
    @Test void petiteReservationSansRemise() {
        Commande c = commande(9); assertEquals(44, commandes.appliquerRemiseGroupe(c, 9), .001);
    }
    @Test void remiseIdempotenteEtEffectifVerifie() {
        Commande c = commande(10);
        assertEquals(39.6, commandes.appliquerRemiseGroupe(c, 10), .001);
        assertEquals(39.6, commandes.appliquerRemiseGroupe(c, 10), .001);
        assertThrows(IllegalArgumentException.class, () -> commandes.appliquerRemiseGroupe(c, 11));
        assertThrows(IllegalArgumentException.class, () -> c.setRemise(1));
        assertThrows(IllegalArgumentException.class, () -> c.setRemise(Double.NaN));
        commandes.cloturerCommande(c); assertThrows(IllegalStateException.class, () -> c.setRemise(0));
    }
    @Test void annuaireEtExceptionVerifiee() throws Exception {
        ClientService service = new ClientService(); service.enregistrer(1, client);
        assertSame(client, service.rechercherParIdentifiant(1)); assertEquals(1, service.lister().size());
        ClientIntrouvableException e = assertThrows(ClientIntrouvableException.class, () -> service.rechercherParIdentifiant(99));
        assertTrue(e.getMessage().contains("99"));
        assertThrows(IllegalArgumentException.class, () -> service.enregistrer(0, client));
        assertThrows(IllegalArgumentException.class, () -> service.rechercherParIdentifiant(0));
    }
    @Test void menuOptionalEtImportPartiel() throws Exception {
        Path fichier = dossier.resolve("menu.txt");
        Files.writeString(fichier, "# Commentaire\n\nENTREE;Salade;8\nmalformee\nPLAT;Poulet;16\nDESSERT;Tarte;7\nINCONNU;X;1\nPLAT;X;abc\n");
        MenuService menu = new MenuService(); assertEquals(3, menu.importer(fichier));
        assertTrue(menu.rechercherArticleParNom("salade").isPresent()); assertTrue(menu.rechercherArticleParNom("Absent").isEmpty());
        assertEquals(3, menu.lister().size()); assertThrows(IllegalArgumentException.class, () -> menu.rechercherArticleParNom(" "));
        assertThrows(java.io.IOException.class, () -> menu.importer(dossier.resolve("absent")));
    }
    @Test void rechercheDisponibiliteEtChargement() throws Exception {
        reservations.reserverTable(date, 4, client, table);
        assertEquals(1, reservations.rechercherTables(List.of(table, new Table(2, 4)), date, 3).size());
        assertFalse(reservations.estDisponible(table, date, 13));
        reservations.charger(List.of()); assertTrue(reservations.estDisponible(table, date, 3));
        assertThrows(IllegalArgumentException.class, () -> reservations.estDisponible(table, date.minusDays(1), 3));
    }
    @Test void modificationDesCoordonneesAvecInvariants() {
        client.setNom("Martin"); client.setTelephone(null); client.setEmail("martin@example.fr");
        assertEquals("Martin", client.getNom()); assertEquals("", client.getTelephone()); assertEquals("martin@example.fr", client.getEmail());
        assertThrows(IllegalArgumentException.class, () -> client.setNom("")); assertThrows(IllegalArgumentException.class, () -> client.setEmail("sans arobase"));
        serveur.setNom("Paul"); serveur.setMatricule("S2"); assertEquals("Paul", serveur.getNom()); assertEquals("S2", serveur.getMatricule());
        assertThrows(IllegalArgumentException.class, () -> serveur.setMatricule("")); assertThrows(IllegalArgumentException.class, () -> serveur.setNom(""));
        table.setNumero(2); table.setCapacite(5); assertEquals(2, table.getNumero()); assertEquals(5, table.getCapacite());
        assertThrows(IllegalArgumentException.class, () -> table.setCapacite(0)); assertThrows(IllegalArgumentException.class, () -> table.setNumero(0));
    }
    @Test void modificationReservationRespecteCapacite() {
        Reservation r = new Reservation(date, 2, client, table); r.setNombrePersonnes(5); r.setDateHeure(date.plusDays(1));
        assertEquals(5, r.getNombrePersonnes()); assertEquals(date.plusDays(1), r.getDateHeure());
        assertThrows(IllegalArgumentException.class, () -> r.setNombrePersonnes(13)); assertThrows(IllegalArgumentException.class, () -> r.setNombrePersonnes(0));
        assertThrows(IllegalArgumentException.class, () -> r.setDateHeure(null));
    }
    @Test void valeursInvalidesRefuseesAvantMutation() {
        Entree e = new Entree("Salade", 8);
        assertThrows(IllegalArgumentException.class, () -> e.setPrixBase(Double.POSITIVE_INFINITY)); assertEquals(8, e.getPrixBase());
        assertThrows(IllegalArgumentException.class, () -> e.setNom(""));
        assertThrows(IllegalArgumentException.class, () -> new Dessert("Tarte", -1));
        assertThrows(IllegalArgumentException.class, () -> new PlatPrincipal("Plat", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new Entree("", 1));
        assertThrows(IllegalArgumentException.class, () -> new Client("", "", "a@b"));
        assertThrows(IllegalArgumentException.class, () -> new Client("Dupont", "", "incorrect"));
        assertThrows(IllegalArgumentException.class, () -> new Table(0, 2));
        assertThrows(IllegalArgumentException.class, () -> new Table(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Serveur("Alice", ""));
        assertThrows(IllegalArgumentException.class, () -> new Serveur("", "S1"));
        assertThrows(IllegalArgumentException.class, () -> commandes.calculerMontantCommande(null));
        assertThrows(IllegalArgumentException.class, () -> commandes.ajouterArticle(commande(2), null, 1));
        assertThrows(IllegalArgumentException.class, () -> commandes.ajouterArticle(commande(2), e, -1));
    }
    @Test void saisiesDeFormulaireValideesParService() {
        assertEquals(date, Validation.dateHeure(date.toLocalDate(), "19:00")); assertEquals(3, Validation.entier("3"));
        assertThrows(IllegalArgumentException.class, () -> Validation.entier("abc"));
        assertThrows(IllegalArgumentException.class, () -> Validation.entier("0"));
        assertThrows(IllegalArgumentException.class, () -> Validation.texte(null, "Nom"));
        assertThrows(IllegalArgumentException.class, () -> Validation.requis(null, "Article"));
    }
}
