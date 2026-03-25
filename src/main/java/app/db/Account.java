package app.db;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.hibernate.exception.ConstraintViolationException;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.TypedQuery;

@Entity
public class Account
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false, unique = true, length = 16)
    public String username;

    String password;

    public Instant created;

    public Account() {}
    public Account(String username, String password)
    {
        this.username = username;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Verifies a username.
     *
     * @param  username the string to be verified
     * @return          true if valid
     */
    public static boolean verifyUsername(String username)
    {
        if (username.length() < 4 ||
            username.length() >= 16)
            return false;

        return Validator.verifyTag(username);
    }

    @PrePersist
    void prePersist()
    {
        if (!verifyUsername(username))
            throw new DBException("invalid username");
        created = Instant.now();
    }

    public static void create(DBContext db, Account account)
        throws DBException
    {
        CRUD.create(db, account);
    }

    public static Account load(DBContext db, Integer id)
            throws DBException
    {
        return CRUD.read(db, Account.class, id);
    }

    public static Account loadByName(DBContext db, String username)
        throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            String JPQL = "SELECT a FROM Account a WHERE a.username=:username";
            TypedQuery<Account> q = em.createQuery(JPQL, Account.class);
            q.setParameter("username", username);
            return q.getSingleResultOrNull();
        } catch (Exception e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static Integer signup(DBContext db, String username, String password)
        throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            Account account;
            account = new Account(username, password);
            em.getTransaction().begin();
            em.persist(account);
            em.getTransaction().commit();
            return account.id;
        } catch (ConstraintViolationException e) {
            return null;
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static Account signin(DBContext db, String username, String password)
        throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            Account account;
            String JPQL = "SELECT a FROM Account a WHERE a.username=:username";
            TypedQuery<Account> q = em.createQuery(JPQL, Account.class);
            q.setParameter("username", username);
            account = q.getSingleResultOrNull();
            if (account == null)
                return null;
            if (!BCrypt.checkpw(password, account.password))
                return null;
            return account;
        } catch (Exception e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }
}
