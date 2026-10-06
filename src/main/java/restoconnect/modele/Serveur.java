package restoconnect.modele;

import jakarta.persistence.*;

@Entity
public class Serveur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected Serveur() { }

    public java.util.Optional<Long> getId() {
        return java.util.Optional.ofNullable(id);
    }


    @Column(nullable = false)
    private String nom;
    @Column(nullable = false, unique = true)
    private String matricule;

    public Serveur(String nom, String matricule) {

        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom du serveur ne peut pas être vide."
            );
        }

        if (matricule == null || matricule.isBlank()) {
            throw new IllegalArgumentException(
                    "Le matricule ne peut pas être vide."
            );
        }

        this.nom = nom;
        this.matricule = matricule;
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

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {

        if (matricule == null || matricule.isBlank()) {
            throw new IllegalArgumentException(
                    "Le matricule ne peut pas être vide."
            );
        }

        this.matricule = matricule;
    }

    @Override
    public String toString() {
        return "Serveur{" +
                "nom='" + nom + '\'' +
                ", matricule='" + matricule + '\'' +
                '}';
    }
}
