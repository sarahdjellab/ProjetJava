package restoconnect.vue;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;
import restoconnect.controleur.EcranControleur;
import restoconnect.service.RestaurantService;
import restoconnect.modele.*;
import java.time.LocalDateTime;
import java.io.IOException;

public class Navigation {
    private final Stage fenetre;
    private final RestaurantService service;
    private LocalDateTime debut;
    private int personnes;
    private Client client;
    private Table table;
    private Commande commande;

    public Navigation(Stage fenetre, RestaurantService service) { this.fenetre = fenetre; this.service = service; }

    public void afficher(String ecran) throws IOException {
        FXMLLoader chargeur = new FXMLLoader(getClass().getResource("/restoconnect/vue/" + ecran + ".fxml"));
        chargeur.setControllerFactory(type -> new EcranControleur(this));
        Parent racine = chargeur.load();
        Scene scene = new Scene(racine, 980, 700);
        scene.getStylesheets().add(getClass().getResource("/restoconnect/vue/style.css").toExternalForm());
        fenetre.setScene(scene);
        fenetre.setTitle("RestoConnect — " + ecran);
        fenetre.show();
    }

    public RestaurantService getService() { return service; }

    public void preparer(LocalDateTime debut, int personnes, Client client, Table table) {
        this.debut = debut; this.personnes = personnes; this.client = client; this.table = table; this.commande = null;
    }

    public LocalDateTime getDebut() { return debut; }

    public int getPersonnes() { return personnes; }

    public Client getClient() { return client; }

    public Table getTable() { return table; }

    public java.util.Optional<Commande> getCommande() { return java.util.Optional.ofNullable(commande); }

    public void setCommande(Commande commande) { this.commande = commande; }

    public Stage getFenetre() { return fenetre; }
}
