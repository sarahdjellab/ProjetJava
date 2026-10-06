package restoconnect;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import restoconnect.service.RestaurantService;
import restoconnect.vue.Navigation;
import java.util.logging.*;

public class ApplicationResto extends Application {
    private static final Logger JOURNAL = Logger.getLogger(ApplicationResto.class.getName());
    private RestaurantService service;

    public ApplicationResto() { }

    @Override public void start(Stage fenetre) {
        try {
            service = new RestaurantService();
            new Navigation(fenetre, service).afficher("accueil");
        } catch (Exception erreur) {
            JOURNAL.log(Level.SEVERE, "Démarrage impossible.", erreur);
            Alert alerte = new Alert(Alert.AlertType.ERROR, "Impossible de démarrer RestoConnect. Vérifiez la configuration de la base de données.");
            alerte.showAndWait(); javafx.application.Platform.exit();
        }
    }

    @Override public void stop() { if (service != null) { service.close(); } }

    public static void main(String[] args) { launch(args); }
}
