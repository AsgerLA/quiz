package app;

import app.service.Service;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

/**
 * Base service test.
 * Custom junit extension
 */
class ServiceTest
        implements BeforeAllCallback, AutoCloseable
{
    private static EntityManagerFactory emf;

    private static boolean started = false;

    @Override
    public void beforeAll(ExtensionContext context)
            throws Exception
    {
        if (started)
            return;
        started = true;

        emf = Service.getEntityManagerFactory();
        TestData.populate(emf);

        // register callback
        context.getRoot().getStore(GLOBAL).put("ServiceTest", this);
    }

    @Override
    public void close()
    {
        // print hibernate metrics
        SessionFactory sf = emf.unwrap(SessionFactory.class);
        Statistics stats = sf.getStatistics();
        System.out.println(stats.toString());

        if (emf != null)
            emf.close();
    }
}
