package restoconnect.persistance;
import restoconnect.modele.*;
import restoconnect.service.Validation;

public final class ValidationEntite {
    private ValidationEntite() { }

    public static void verifier(Object entite) {
        Validation.requis(entite, "Entité");
        if (entite instanceof Client client) {
            new Client(client.getNom(), client.getTelephone(), client.getEmail());
        } else if (entite instanceof Serveur serveur) {
            new Serveur(serveur.getNom(), serveur.getMatricule());
        } else if (entite instanceof Table table) {
            new Table(table.getNumero(), table.getCapacite());
        } else if (entite instanceof ArticleMenu article) {
            Validation.texte(article.getNom(), "Nom");
            if (!Double.isFinite(article.getPrixBase()) || article.getPrixBase() <= 0
                    || !Double.isFinite(article.calculerPrixTTC()) || article.calculerPrixTTC() <= 0) {
                throw new IllegalArgumentException("Prix invalide.");
            }
        } else if (entite instanceof Reservation reservation) {
            new Reservation(reservation.getDateHeure(), reservation.getNombrePersonnes(), reservation.getClient(), reservation.getTable());
            verifier(reservation.getClient());
            verifier(reservation.getTable());
        } else if (entite instanceof Commande commande) {
            verifier(commande.getReservation());
            verifier(commande.getServeur());
            if (commande.getLigneCommandes().isEmpty()) { throw new IllegalArgumentException("Une commande vide ne peut pas être sauvegardée."); }
            commande.getLigneCommandes().forEach(l -> { verifier(l.getArticleMenu()); Validation.positif(l.getQuantite()); });
        } else { throw new IllegalArgumentException("Type d'entité non pris en charge."); }
    }
}
