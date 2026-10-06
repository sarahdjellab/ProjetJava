package restoconnect.service;
import restoconnect.modele.*;

public class CommandeService {
    private static final int SEUIL_GROUPE = 10;
    private static final double TAUX_REMISE_GROUPE = 0.10;

    public CommandeService() { }

    public double calculerPrixArticle(ArticleMenu article) { return Validation.requis(article, "Article").calculerPrixTTC(); }

    public Commande creerCommande(Reservation reservation, Serveur serveur, ArticleMenu article, int quantite) {
        Validation.requis(reservation, "Réservation");
        Validation.requis(serveur, "Serveur");
        Validation.requis(article, "Article");
        Validation.positif(quantite);
        return new Commande(reservation, serveur, article, quantite);
    }

    public void ajouterArticle(Commande commande, ArticleMenu article, int quantite) {
        Validation.requis(commande, "Commande");
        Validation.requis(article, "Article");
        Validation.positif(quantite);
        commande.ajouterLigne(article, quantite);
    }

    public double calculerMontantCommande(Commande commande) {
        Validation.requis(commande, "Commande");
        if (commande.getLigneCommandes().isEmpty()) { throw new IllegalArgumentException("Une commande sans ligne est interdite."); }
        return commande.calculerTotal();
    }

    public double cloturerCommande(Commande commande) {
        Validation.requis(commande, "Commande");
        commande.cloturer();
        return commande.calculerTotal();
    }

    public double appliquerRemiseGroupe(Commande commande, int nombrePersonnes) {
        Validation.requis(commande, "Commande");
        Validation.positif(nombrePersonnes);
        if (nombrePersonnes != commande.getReservation().getNombrePersonnes()) {
            throw new IllegalArgumentException("L'effectif doit correspondre à la réservation.");
        }
        commande.setRemise(nombrePersonnes >= SEUIL_GROUPE ? TAUX_REMISE_GROUPE : 0);
        return commande.calculerTotal();
    }
}
