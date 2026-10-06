package restoconnect;
import org.junit.jupiter.api.Test;
import restoconnect.modele.*;
import restoconnect.service.CommandeService;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class RemiseGroupeTest {
    @Test void dixConvivesBeneficientDeDixPourCent() {
        Commande commande = new Commande(new Reservation(LocalDateTime.of(2030, 1, 1, 20, 0),
                10, new Client("Dupont", "", "a@b.fr"), new Table(1, 10)),
                new Serveur("Alice", "S1"), new PlatPrincipal("Plat", 20), 10);
        assertEquals(198, new CommandeService().appliquerRemiseGroupe(commande, 10), 0.001);
    }
}
