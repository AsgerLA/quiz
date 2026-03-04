package app.db;

import jakarta.persistence.EntityManagerFactory;

public class DBContext
{
    public DBContext(String username, String password, String url)
            throws DBException
    {
        try {
            emf = HibernateConfig.createEntityManagerFactory(username, password, url);
        } catch (Exception e) {
            throw new DBException(e.getMessage());
        }
    }

    EntityManagerFactory emf;
}
