package restoconnect.modele;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected Reservation() { }

    public java.util.Optional<Long> getId() {
        return java.util.Optional.ofNullable(id);
    }


    @Column(nullable = false)
    private LocalDateTime dateHeure;
    @Column(nullable = false)
    private int nombrePersonnes;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Client client;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Table table;

    public Reservation(
            LocalDateTime dateHeure,
            int nombrePersonnes,
            Client client,
            Table table) {

        if (dateHeure == null) {
            throw new IllegalArgumentException(
                    "La date et l'heure sont obligatoires."
            );
        }

        if (nombrePersonnes <= 0) {
            throw new IllegalArgumentException(
                    "Le nombre de personnes doit être positif."
            );
        }

        if (client == null) {
            throw new IllegalArgumentException(
                    "Le client est obligatoire."
            );
        }

        if (table == null) {
            throw new IllegalArgumentException(
                    "La table est obligatoire."
            );
        }

        if (nombrePersonnes > table.getCapacite()) {
            throw new IllegalArgumentException(
                    "Le nombre de personnes dépasse la capacité de la table."
            );
        }

        this.dateHeure = dateHeure;
        this.nombrePersonnes = nombrePersonnes;
        this.client = client;
        this.table = table;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {

        if (dateHeure == null) {
            throw new IllegalArgumentException(
                    "La date est obligatoire."
            );
        }

        this.dateHeure = dateHeure;
    }

    public int getNombrePersonnes() {
        return nombrePersonnes;
    }

    public void setNombrePersonnes(int nombrePersonnes) {

        if (nombrePersonnes <= 0) {
            throw new IllegalArgumentException(
                    "Le nombre de personnes doit être positif."
            );
        }

        if (table != null && nombrePersonnes > table.getCapacite()) {
            throw new IllegalArgumentException(
                    "Le nombre de personnes dépasse la capacité."
            );
        }

        this.nombrePersonnes = nombrePersonnes;
    }

    public Client getClient() {
        return client;
    }

    public Table getTable() {
        return table;
    }

    public LocalDateTime getFin() {
        return dateHeure.plusHours(2);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "dateHeure=" + dateHeure +
                ", nombrePersonnes=" + nombrePersonnes +
                ", client=" + client.getNom() +
                ", table=" + table.getNumero() +
                '}';
    }
}
