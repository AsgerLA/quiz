package app.db;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

/**
 * Base DB test.
 * Custom junit extension
 */
class DBTest
        implements BeforeAllCallback, AutoCloseable
{
    static DBContext db;

    private static boolean started = false;

    @Override
    public void beforeAll(ExtensionContext context)
            throws Exception
    {
        if (started)
            return;
        started = true;

        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");
        String url      = System.getenv("DB_URL");
        db = new DBContext(username, password, url);
        TestData.populate(db);

        // register callback
        context.getRoot().getStore(GLOBAL).put("DBTest", this);
    }

    @Override
    public void close()
    {
        // print hibernate metrics
        EntityManagerFactory emf = db.emf;
        SessionFactory sf = emf.unwrap(SessionFactory.class);
        Statistics stats = sf.getStatistics();
        System.out.println(stats.toString());

        emf.close();
    }
}
