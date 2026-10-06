package restoconnect.modele;
import jakarta.persistence.*;
import java.util.*;

@Entity
@DiscriminatorValue("DESSERT")
public class Dessert extends ArticleMenu  {
    protected Dessert() { }
    public Dessert(String nom, double prixBase) { super(nom, prixBase); }
    public Dessert(String nom) { this(nom, 10.0); }
    @Override
    public double calculerPrixTTC() { return (getPrixBase()) * 1.2; }
}
