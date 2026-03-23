package app.db;

import java.util.Properties;

import jakarta.persistence.EntityManagerFactory;

public class DBContext
{
    private DBContext() {}

    public static DBContext create(String username, String password, String url)
        throws DBException
    {
            try {
                DBContext db = new DBContext();
                Properties props = defaultProps();
                props.setProperty("hibernate.connection.username", username);
                props.setProperty("hibernate.connection.password", password);
                props.setProperty("hibernate.connection.url", url);

                db.emf = HibernateConfig.createEntityManagerFactory(props);

                return db;
        } catch (Exception e) {
            throw new DBException(e.getMessage());
        }
    }

    public static DBContext createTEST(String username, String password, String url)
        throws DBException
    {
            try {
                DBContext db = new DBContext();
                Properties props = defaultProps();

                props.setProperty("hibernate.hbm2ddl.auto", "create");

                props.setProperty("hibernate.connection.username", username);
                props.setProperty("hibernate.connection.password", password);
                props.setProperty("hibernate.connection.url", url);

                db.emf = HibernateConfig.createEntityManagerFactory(props);

                return db;
        } catch (Exception e) {
            throw new DBException(e.getMessage());
        }
    }

    EntityManagerFactory emf;

    private static Properties defaultProps()
    {
        Properties props = new Properties();
        props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.put("hibernate.current_session_context_class", "thread");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.use_sql_comments", "false");
        props.put("hibernate.hikari.maximumPoolSize", "10");
        props.put("hibernate.hikari.minimumIdle", "2");
        props.put("hibernate.hikari.connectionTimeout", "20000");
        props.put("hibernate.generate_statistics", "true");
        props.put("hibernate.hbm2ddl.auto", "update");
        return props;
    }
}
