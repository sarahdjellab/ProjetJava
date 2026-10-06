package restoconnect.service;
import restoconnect.modele.Client;
import restoconnect.exceptions.ClientIntrouvableException;
import java.util.*;

public class ClientService {
    private final Map<Long, Client> clients = new LinkedHashMap<>();

    public ClientService() { }

    public void enregistrer(long id, Client client) {
        if (id <= 0) { throw new IllegalArgumentException("Identifiant invalide."); }
        Validation.requis(client, "Client");
        clients.put(id, client);
    }

    public Client rechercherParIdentifiant(long id) throws ClientIntrouvableException {
        if (id <= 0) { throw new IllegalArgumentException("Identifiant invalide."); }
        Client client = clients.get(id);
        if (client == null) { throw new ClientIntrouvableException("Aucun client d'identifiant " + id + "."); }
        return client;
    }

    public List<Client> lister() { return List.copyOf(clients.values()); }
}
