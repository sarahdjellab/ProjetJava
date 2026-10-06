package restoconnect.service;
import java.time.*;

public final class Validation {
    private Validation() { }
    public static <T> T requis(T valeur, String nom) {
        if (valeur == null) { throw new IllegalArgumentException(nom + " est obligatoire."); }
        return valeur;
    }

    public static String texte(String valeur, String nom) {
        if (valeur == null || valeur.isBlank()) { throw new IllegalArgumentException(nom + " est obligatoire."); }
        return valeur.trim();
    }

    public static int positif(int valeur) {
        if (valeur <= 0) { throw new IllegalArgumentException("Le nombre doit être strictement positif."); }
        return valeur;
    }

    public static int entier(String texte) { return positif(Integer.parseInt(texte(texte, "Nombre"))); }

    public static LocalDateTime dateHeure(LocalDate date, String heure) {
        return LocalDateTime.of(requis(date, "Date"), LocalTime.parse(texte(heure, "Heure")));
    }
}
