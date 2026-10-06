package restoconnect.service;
import restoconnect.modele.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

public class MenuService {
    private static final Logger JOURNAL = Logger.getLogger(MenuService.class.getName());
    private final List<ArticleMenu> articles = new ArrayList<>();

    public MenuService() { }

    public void ajouter(ArticleMenu article) { articles.add(Validation.requis(article, "Article")); }

    public List<ArticleMenu> lister() { return List.copyOf(articles); }

    public Optional<ArticleMenu> rechercherArticleParNom(String nom) {
        String recherche = Validation.texte(nom, "Nom");
        return articles.stream().filter(a -> a.getNom().equalsIgnoreCase(recherche)).findFirst();
    }

    public int importer(Path chemin) throws IOException {
        Validation.requis(chemin, "Fichier");
        int nombre = 0;
        try (BufferedReader lecteur = Files.newBufferedReader(chemin, StandardCharsets.UTF_8)) {
            String ligne;
            int numero = 0;
            while ((ligne = lecteur.readLine()) != null) {
                numero++;
                if (ligne.isBlank() || ligne.startsWith("#")) { continue; }
                try {
                    String[] champs = ligne.split(";", -1);
                    if (champs.length != 3) { throw new IllegalArgumentException("Trois colonnes attendues."); }
                    double prix = Double.parseDouble(champs[2].trim());
                    ArticleMenu article = switch (champs[0].trim().toUpperCase(Locale.ROOT)) {
                        case "ENTREE" -> new Entree(champs[1], prix);
                        case "PLAT" -> new PlatPrincipal(champs[1], prix);
                        case "DESSERT" -> new Dessert(champs[1], prix);
                        default -> throw new IllegalArgumentException("Catégorie inconnue.");
                    };
                    ajouter(article);
                    nombre++;
                } catch (IllegalArgumentException erreur) {
                    JOURNAL.log(Level.WARNING, "Ligne " + numero + " ignorée : " + erreur.getMessage(), erreur);
                }
            }
        } catch (IOException erreur) {
            JOURNAL.log(Level.SEVERE, "Impossible de lire le menu.", erreur);
            throw erreur;
        }
        return nombre;
    }
}
