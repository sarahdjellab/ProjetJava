package restoconnect.modele;
import java.util.Map;
public interface Personnalisable {
    void ajouterSupplement(String designation, double prix);
    Map<String, Double> listerSupplements();
}
