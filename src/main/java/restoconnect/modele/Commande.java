package restoconnect.modele;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected Commande() { }

    public java.util.Optional<Long> getId() {
        return java.util.Optional.ofNullable(id);
    }


    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private Reservation reservation;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Serveur serveur;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lignes_commande", joinColumns = @JoinColumn(name = "commande_id"))
    @OrderColumn(name = "position_ligne")
    private List<LigneCommande> ligneCommandes = new ArrayList<>();

    @Column(nullable = false)
    private boolean cloturee;
    @Column(nullable = false)
    private double remise;
    @Column(nullable = false)
    private double montantFacture;

    public Commande(
            Reservation reservation,
            Serveur serveur,
            ArticleMenu article,
            int quantite) {

        if (reservation == null) {
            throw new IllegalArgumentException(
                    "La réservation est obligatoire."
            );
        }

        if (serveur == null) {
            throw new IllegalArgumentException(
                    "Le serveur est obligatoire."
            );
        }

        if (article == null) {
            throw new IllegalArgumentException(
                    "L'article est obligatoire."
            );
        }

        if (quantite <= 0) {
            throw new IllegalArgumentException(
                    "La quantité doit être strictement positive."
            );
        }

        this.reservation = reservation;
        this.serveur = serveur;
        this.ligneCommandes = new ArrayList<>();

        this.ligneCommandes.add(
                new LigneCommande(article, quantite)
        );
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Serveur getServeur() {
        return serveur;
    }

    public List<LigneCommande> getLigneCommandes() {
        return Collections.unmodifiableList(ligneCommandes);
    }

    public void ajouterLigne(ArticleMenu article, int quantite) {

        if (article == null) {
            throw new IllegalArgumentException(
                    "L'article est obligatoire."
            );
        }

        if (quantite <= 0) {
            throw new IllegalArgumentException(
                    "La quantité doit être strictement positive."
            );
        }

        if (cloturee) {
            throw new IllegalStateException("Une commande clôturée ne peut plus être modifiée.");
        }
        ligneCommandes.add(
                new LigneCommande(article, quantite)
        );
    }

    public double calculerTotal() {

        double total = 0;

        for (LigneCommande ligne : ligneCommandes) {
            total += ligne.calculerMontant();
        }

        assert !ligneCommandes.isEmpty() : "Une commande doit contenir une ligne.";
        return cloturee ? montantFacture : total * (1 - remise);
    }

    public boolean isCloturee() { return cloturee; }

    public void setRemise(double remise) {
        if (!Double.isFinite(remise) || remise < 0 || remise >= 1) {
            throw new IllegalArgumentException("La remise doit être comprise entre 0 et 1 exclu.");
        }
        if (cloturee) { throw new IllegalStateException("La commande est déjà clôturée."); }
        this.remise = remise;
    }

    public void cloturer() {
        if (cloturee) { throw new IllegalStateException("La commande est déjà clôturée."); }
        if (ligneCommandes.isEmpty()) { throw new IllegalStateException("Une commande vide ne peut pas être clôturée."); }
        montantFacture = calculerTotal();
        cloturee = true;
    }

    @Embeddable
    public static class LigneCommande {

        @ManyToOne(optional = false, fetch = FetchType.EAGER)
        private ArticleMenu articleMenu;
        @Column(nullable = false)
        private double prixUnitaireTTC;

        protected LigneCommande() { }
        @Column(nullable = false)
        private int quantite;

        private LigneCommande(
                ArticleMenu articleMenu,
                int quantite) {

            if (articleMenu == null) {
                throw new IllegalArgumentException(
                        "L'article est obligatoire."
                );
            }

            if (quantite <= 0) {
                throw new IllegalArgumentException(
                        "La quantité doit être positive."
                );
            }

            this.articleMenu = articleMenu;
            this.prixUnitaireTTC = articleMenu.calculerPrixTTC();
            this.quantite = quantite;
        }

        public ArticleMenu getArticleMenu() {
            return articleMenu;
        }

        public int getQuantite() {
            return quantite;
        }

        public double calculerMontant() {
            return prixUnitaireTTC * quantite;
        }
    }
}
