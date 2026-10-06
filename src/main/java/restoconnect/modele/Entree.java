package restoconnect.modele;
import jakarta.persistence.*;
import java.util.*;
@Entity
@DiscriminatorValue("ENTREE")
public class Entree extends ArticleMenu implements Personnalisable {
    protected Entree() { }
    public Entree(String nom, double prixBase) { super(nom, prixBase); }
    public Entree(String nom) { this(nom, 10.0); }
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "supplements", joinColumns = @JoinColumn(name = "article_id"))
    @MapKeyColumn(name = "designation")
    @Column(name = "prix", nullable = false)
    private Map<String, Double> supplements = new LinkedHashMap<>();
    @Override
    public void ajouterSupplement(String designation, double prix) {
        if (designation == null || designation.isBlank() || !Double.isFinite(prix) || prix <= 0) {
            throw new IllegalArgumentException("Le supplément doit avoir un nom et un prix positif fini.");
        }
        supplements.put(designation.trim(), prix);
    }
    @Override
    public Map<String, Double> listerSupplements() { return Map.copyOf(supplements); }
    @Override
    public double calculerPrixTTC() { return (getPrixBase() + supplements.values().stream().mapToDouble(Double::doubleValue).sum()) * 1.05; }
}
