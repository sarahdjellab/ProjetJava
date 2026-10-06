package restoconnect;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import restoconnect.modele.*;
import restoconnect.service.*;
import restoconnect.vue.Navigation;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.logging.*;

public final class CaptureEcrans {
    private static final Logger JOURNAL = Logger.getLogger(CaptureEcrans.class.getName());
    private static Path dossier;
    private CaptureEcrans() { }

    public static void main(String[] args) {
        dossier = Path.of(args[0]);
        Platform.startup(() -> Platform.runLater(() -> {
            try (RestaurantService service = new RestaurantService(PersistanceTest.creerFabrique())) {
                Files.createDirectories(dossier);
                Stage stage = new Stage(); Navigation navigation = new Navigation(stage, service);
                navigation.afficher("accueil"); photo(stage, "01-accueil");
                navigation.afficher("plan"); bouton(stage, "Rechercher").fire();
                photo(stage, "02-plan-salle");
                @SuppressWarnings("unchecked") TableView<Table> tables = (TableView<Table>) stage.getScene().lookup("#tables");
                tables.getSelectionModel().selectFirst(); bouton(stage, "Choisir").fire();
                photo(stage, "03-commande");
                TextField quantite = (TextField) stage.getScene().lookup("#quantite");
                quantite.setText(""); bouton(stage, "Ajouter").fire(); photo(stage, "05-erreur-formulaire");
                quantite.setText("2"); bouton(stage, "Ajouter").fire(); bouton(stage, "Clôturer").fire();
                photo(stage, "04-facture");
                navigation.afficher("plan"); bouton(stage, "Rechercher").fire();
                @SuppressWarnings("unchecked") TableView<Table> libres = (TableView<Table>) stage.getScene().lookup("#tables");
                libres.getSelectionModel().selectFirst(); bouton(stage, "Choisir").fire();
                service.reserverEtCommander(navigation.getDebut(), navigation.getPersonnes(), navigation.getClient(), navigation.getTable(),
                        service.listerServeurs().getFirst(), service.listerArticles().getFirst(), "1");
                Platform.runLater(() -> {
                    try {
                        for (Window fenetre : java.util.List.copyOf(Window.getWindows())) {
                            if (fenetre != stage && fenetre.isShowing()) { photo(fenetre, "06-alerte-metier"); fenetre.hide(); }
                        }
                    } catch (Exception erreur) { JOURNAL.log(Level.SEVERE, "Capture de l'alerte impossible.", erreur); }
                });
                bouton(stage, "Ajouter").fire(); stage.close();
            } catch (Exception erreur) {
                JOURNAL.log(Level.SEVERE, "Contrôle des écrans impossible.", erreur);
                System.exit(1);
            } finally { Platform.exit(); }
        }));
    }
    private static Button bouton(Stage stage, String prefixe) {
        return trouver(stage.getScene().getRoot(), prefixe);
    }
    private static Button trouver(Parent parent, String prefixe) {
        for (Node enfant : parent.getChildrenUnmodifiable()) {
            if (enfant instanceof Button bouton && bouton.getText().startsWith(prefixe)) { return bouton; }
            if (enfant instanceof Parent sousParent) {
                Button resultat = trouver(sousParent, prefixe); if (resultat != null) { return resultat; }
            }
        }
        return null;
    }
    private static void photo(Window fenetre, String nom) throws Exception {
        Parent racine = fenetre.getScene().getRoot(); racine.applyCss(); racine.layout();
        WritableImage image = racine.snapshot(null, null);
        BufferedImage png = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < png.getHeight(); y++) {
            for (int x = 0; x < png.getWidth(); x++) { png.setRGB(x, y, image.getPixelReader().getArgb(x, y)); }
        }
        ImageIO.write(png, "png", dossier.resolve(nom + ".png").toFile());
    }
}
