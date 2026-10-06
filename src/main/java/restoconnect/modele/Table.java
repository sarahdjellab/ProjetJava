package restoconnect.modele;

import jakarta.persistence.*;

@Entity
@jakarta.persistence.Table(name = "tables_restaurant")
public class Table {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected Table() { }

    public java.util.Optional<Long> getId() {
        return java.util.Optional.ofNullable(id);
    }


    @Column(nullable = false, unique = true)
    private int numero;
    @Column(nullable = false)
    private int capacite;

    public Table(int numero, int capacite) {

        if (numero <= 0) {
            throw new IllegalArgumentException(
                    "Le numéro de table doit être positif."
            );
        }

        if (capacite <= 0) {
            throw new IllegalArgumentException(
                    "La capacité doit être strictement positive."
            );
        }

        this.numero = numero;
        this.capacite = capacite;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {

        if (numero <= 0) {
            throw new IllegalArgumentException(
                    "Le numéro doit être positif."
            );
        }

        this.numero = numero;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {

        if (capacite <= 0) {
            throw new IllegalArgumentException(
                    "La capacité doit être strictement positive."
            );
        }

        this.capacite = capacite;
    }

    @Override
    public String toString() {
        return "Table " + numero +
                " (" + capacite + " personnes)";
    }
}
