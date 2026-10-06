package restoconnect.controleur;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.StringConverter;
import restoconnect.modele.*;
import restoconnect.vue.Navigation;
import restoconnect.service.Validation;
import restoconnect.exceptions.*;
import java.time.*;
import java.util.logging.*;

public class EcranControleur {
    private static final Logger JOURNAL = Logger.getLogger(EcranControleur.class.getName());
    private final Navigation navigation;
    @FXML private DatePicker date;
    @FXML private TextField heure, personnes, quantite, nomClient, telephoneClient, emailClient;
    @FXML private ComboBox<Client> clients;
    @FXML private ComboBox<Serveur> serveurs;
    @FXML private ComboBox<ArticleMenu> articles;
    @FXML private ComboBox<Commande> historique;
    @FXML private TableView<Table> tables;
    @FXML private TableColumn<Table, String> numero, capacite;
    @FXML private TableView<Commande.LigneCommande> lignes;
    @FXML private TableColumn<Commande.LigneCommande, String> designation, nombre, montantLigne;
    @FXML private Label total, informations, erreur;
    @FXML private Pane formulaire;

    public EcranControleur(Navigation navigation) { this.navigation = navigation; }

    @FXML public void initialize() {
        if (date != null) {
            date.setValue(LocalDate.now().plusDays(1)); heure.setText("19:30"); personnes.setText("2");
            numero.setCellValueFactory(c -> new SimpleStringProperty(Integer.toString(c.getValue().getNumero())));
            capacite.setCellValueFactory(c -> new SimpleStringProperty(Integer.toString(c.getValue().getCapacite())));
            clients.setItems(FXCollections.observableArrayList(navigation.getService().listerClients()));
            convertir(clients, Client::getNom); clients.getSelectionModel().selectFirst();
        }
        if (articles != null) {
            articles.setItems(FXCollections.observableArrayList(navigation.getService().listerArticles()));
            convertir(articles, ArticleMenu::getNom); articles.getSelectionModel().selectFirst();
            serveurs.setItems(FXCollections.observableArrayList(navigation.getService().listerServeurs()));
            convertir(serveurs, Serveur::getNom); serveurs.getSelectionModel().selectFirst(); quantite.setText("1");
        }
        if (lignes != null) {
            designation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArticleMenu().getNom()));
            nombre.setCellValueFactory(c -> new SimpleStringProperty(Integer.toString(c.getValue().getQuantite())));
            montantLigne.setCellValueFactory(c -> new SimpleStringProperty(String.format(java.util.Locale.FRANCE, "%.2f €", c.getValue().calculerMontant())));
            actualiserCommande();
        }
        if (historique != null) {
            historique.setItems(FXCollections.observableArrayList(navigation.getService().listerCommandes()));
            convertir(historique, c -> "Commande " + c.getId().orElseThrow() + " — " + c.getReservation().getClient().getNom() + (c.isCloturee() ? " — facturée" : " — ouverte"));
        }
    }
    private <T> void convertir(ComboBox<T> boite, java.util.function.Function<T, String> texte) {
        boite.setConverter(new StringConverter<>() {
            @Override public String toString(T objet) { return objet == null ? "" : texte.apply(objet); }
            @Override public T fromString(String texte) { throw new UnsupportedOperationException("Liste non éditable."); }
        });
    }

    @FXML public void ouvrirPlan() { executer(() -> navigation.afficher("plan")); }

    @FXML public void ouvrirAccueil() { executer(() -> navigation.afficher("accueil")); }

    @FXML public void rechercher() {
        executer(() -> tables.setItems(FXCollections.observableArrayList(navigation.getService().rechercherTables(date.getValue(), heure.getText(), personnes.getText()))));
    }

    @FXML public void reserver() {
        executer(() -> {
            Table table = tables.getSelectionModel().getSelectedItem();
            LocalDateTime debut = navigation.getService().preparerReservation(date.getValue(), heure.getText(), personnes.getText(), clients.getValue(), table);
            navigation.preparer(debut, Validation.entier(personnes.getText()), clients.getValue(), table);
            navigation.afficher("commande");
        });
    }
    /** Enregistre un nouveau client par la couche métier. */
    @FXML public void creerClient() {
        executer(() -> {
            Client client = navigation.getService().creerClient(nomClient.getText(), telephoneClient.getText(), emailClient.getText());
            clients.getItems().add(client); clients.setValue(client);
            nomClient.clear(); telephoneClient.clear(); emailClient.clear();
        });
    }

    @FXML public void ajouter() {
        executer(() -> {
            Commande commande;
            if (navigation.getCommande().isPresent()) {
                commande = navigation.getService().ajouterLigne(navigation.getCommande().orElseThrow(), articles.getValue(), quantite.getText());
            } else {
                commande = navigation.getService().reserverEtCommander(navigation.getDebut(), navigation.getPersonnes(), navigation.getClient(), navigation.getTable(), serveurs.getValue(), articles.getValue(), quantite.getText());
            }
            navigation.setCommande(commande); serveurs.setDisable(true); actualiserCommande();
        });
    }

    @FXML public void facturer() {
        executer(() -> {
            Commande commande = navigation.getCommande().orElseThrow(() -> new IllegalArgumentException("Ajoutez au moins une ligne avant de facturer."));
            navigation.setCommande(navigation.getService().facturer(commande)); navigation.afficher("facture");
        });
    }

    @FXML public void reprendre() {
        executer(() -> {
            Commande commande = Validation.requis(historique.getValue(), "Commande");
            navigation.setCommande(commande); navigation.afficher(commande.isCloturee() ? "facture" : "commande");
        });
    }
    private void actualiserCommande() {
        navigation.getCommande().ifPresent(c -> {
            lignes.setItems(FXCollections.observableArrayList(c.getLigneCommandes()));
            total.setText("Total TTC : " + navigation.getService().montant(c));
            informations.setText("Table " + c.getReservation().getTable().getNumero() + " • " + c.getReservation().getClient().getNom() + " • " + c.getReservation().getNombrePersonnes() + " convives");
            if (serveurs != null) { serveurs.setValue(c.getServeur()); serveurs.setDisable(true); }
        });
    }
    private void executer(Action action) {
        try {
            if (erreur != null) { erreur.setText(""); }
            if (formulaire != null) { formulaire.getStyleClass().remove("invalide"); }
            action.effectuer();
        } catch (TableIndisponibleException | ClientIntrouvableException probleme) {
            JOURNAL.log(Level.WARNING, probleme.getMessage(), probleme); alerter(Alert.AlertType.WARNING, "Demande refusée", probleme.getMessage());
        } catch (IllegalArgumentException | IllegalStateException | java.time.DateTimeException probleme) {
            JOURNAL.log(Level.WARNING, "Formulaire invalide.", probleme);
            if (erreur != null) { erreur.setText(probleme.getMessage()); }
            if (formulaire != null && !formulaire.getStyleClass().contains("invalide")) { formulaire.getStyleClass().add("invalide"); }
        } catch (Exception probleme) {
            JOURNAL.log(Level.SEVERE, "Opération impossible.", probleme);
            alerter(Alert.AlertType.ERROR, "Opération impossible", "L'opération a échoué. Vos données enregistrées sont conservées. Consultez le journal pour le diagnostic.");
        }
    }
    private void alerter(Alert.AlertType type, String titre, String message) {
        Alert alerte = new Alert(type); alerte.initOwner(navigation.getFenetre()); alerte.setTitle(titre); alerte.setHeaderText(titre); alerte.setContentText(message); alerte.showAndWait();
    }
    @FunctionalInterface private interface Action { void effectuer() throws Exception; }
}
