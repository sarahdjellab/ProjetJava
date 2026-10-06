package restoconnect;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import java.nio.file.*;

public final class ExporterSchema {
    private ExporterSchema() { }

    public static void main(String[] args) throws Exception {
        try (SessionFactory fabrique = PersistanceTest.creerFabrique(); Session session = fabrique.openSession()) {
            var lignes = session.createNativeQuery("SCRIPT NODATA", String.class).getResultList();
            StringBuilder sql = new StringBuilder("-- Schéma H2 réellement généré par les annotations JPA.\\n".replace("\\n", "\n"));
            lignes.stream().filter(s -> !s.startsWith("CREATE USER") && !s.startsWith("SET ") && !s.startsWith("--"))
                    .forEach(s -> sql.append(s).append('\n'));
            Files.writeString(Path.of(args[0]), sql);
        }
    }
}
