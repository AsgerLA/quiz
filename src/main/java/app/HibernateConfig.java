package app;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import jakarta.persistence.EntityManagerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class HibernateConfig
{

    private HibernateConfig() {
    }

    static void registerEntities(Configuration configuration) {
        configuration.addAnnotatedClass(User.class);
        // TODO: Add more entities here...
    }

    private static void loadProps(Properties props, String filename)
            throws IOException
    {
        try (InputStream is = HibernateConfig.class.getClassLoader().getResourceAsStream(filename)) {
            props.load(is);
        }
    }

    static EntityManagerFactory createEntityManagerFactory(String config)
    {
        EntityManagerFactory emf;
        try {
            Properties props = new Properties();
            props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
            props.put("hibernate.current_session_context_class", "thread");
            props.put("hibernate.show_sql", "false");
            props.put("hibernate.format_sql", "false");
            props.put("hibernate.use_sql_comments", "false");
            props.put("hibernate.hikari.maximumPoolSize", "10");
            props.put("hibernate.hikari.minimumIdle", "2");
            props.put("hibernate.hikari.connectionTimeout", "20000");
            props.put("hibernate.generate_statistics", "false");
            props.put("hibernate.hbm2ddl.auto", "create");

            Properties dbprops = new Properties();
            try (InputStream is = new FileInputStream("database.properties")) {
                dbprops.load(is);
            }
            String username = dbprops.getProperty("DB_USERNAME");
            String password = dbprops.getProperty("DB_PASSWORD");
            String url      = dbprops.getProperty("DB_URL");
            props.setProperty("hibernate.connection.username", username);
            props.setProperty("hibernate.connection.password", password);
            props.setProperty("hibernate.connection.url", url);
            if (config != null)
                loadProps(props, config);

            Configuration configuration = new Configuration();
            configuration.setProperties(props);

            // Register entities to make Hibernate aware
            registerEntities(configuration);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            // Make JPA compliant version of Hibernates sf
            SessionFactory sf = configuration.buildSessionFactory(serviceRegistry);
            emf = sf.unwrap(EntityManagerFactory.class);
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }

        return emf;
    }
}
