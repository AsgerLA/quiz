package app.db;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PrePersist;

@Entity
public class Account
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false, unique = true)
    public String username;

    // TODO: password

    public Instant created;

    public Account() {}
    public Account(String username)
    {
        this.username = username;
    }

    @PrePersist
    void prePersist()
    {
        created = Instant.now();
    }

    public static void create(DBContext db, Account account)
        throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(account);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            // TODO: account already exists
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static Account read(DBContext db, Integer id)
        throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            return em.find(Account.class, id);
        } catch (PersistenceException e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }
}
