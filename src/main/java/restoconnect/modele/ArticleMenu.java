package restoconnect.modele;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "categorie")
public abstract class ArticleMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected ArticleMenu() { }

    public java.util.Optional<Long> getId() {
        return java.util.Optional.ofNullable(id);
    }

    @Column(nullable = false)
    private String nom;
    @Column(nullable = false)
    private double prixBase;

    public ArticleMenu(String nom, double prixBase) {

        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom de l'article ne peut pas être vide."
            );
        }

        if (!Double.isFinite(prixBase) || prixBase <= 0) {
            throw new IllegalArgumentException(
                    "Le prix doit être strictement positif."
            );
        }

        this.nom = nom;
        this.prixBase = prixBase;
    }

    public ArticleMenu(String nom) {
        this(nom, 10.0);
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {

        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom ne peut pas être vide."
            );
        }

        this.nom = nom;
    }

    public double getPrixBase() {
        return prixBase;
    }

    public void setPrixBase(double prixBase) {

        if (!Double.isFinite(prixBase) || prixBase <= 0) {
            throw new IllegalArgumentException(
                    "Le prix doit être strictement positif."
            );
        }

        this.prixBase = prixBase;
    }

    public abstract double calculerPrixTTC();

    @Override
    public String toString() {
        return "ArticleMenu{" +
                "nom='" + nom + '\'' +
                ", prixBase=" + prixBase +
                '}';
    }
}
