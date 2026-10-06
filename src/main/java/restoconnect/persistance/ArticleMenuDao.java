package restoconnect.persistance;
import org.hibernate.SessionFactory;
import restoconnect.modele.ArticleMenu;

public class ArticleMenuDao extends Dao<ArticleMenu> {
    public ArticleMenuDao(SessionFactory fabrique) { super(fabrique, ArticleMenu.class); }
}
