package restoconnect.modele;

import jakarta.persistence.*;

@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected Client() { }

    public java.util.Optional<Long> getId() {
        return java.util.Optional.ofNullable(id);
    }


    @Column(nullable = false)
    private String nom;
    @Column(nullable = false)
    private String telephone;
    @Column(nullable = false)
    private String email;

    public Client(String nom, String telephone, String email) {

        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom du client ne peut pas être vide."
            );
        }

        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException(
                    "L'adresse e-mail doit contenir un @."
            );
        }

        this.nom = nom;
        this.telephone = telephone == null ? "" : telephone;
        this.email = email;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {

        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom du client ne peut pas être vide."
            );
        }

        this.nom = nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone == null ? "" : telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {

        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException(
                    "L'adresse e-mail doit contenir un @."
            );
        }

        this.email = email;
    }

    @Override
    public String toString() {
        return "Client{" +
                "nom='" + nom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
