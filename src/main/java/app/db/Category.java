package app.db;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.*;

@Entity
public class Category
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @ManyToOne
    @JoinColumn(nullable = false)
    public Tag tag;

    public Category() {}
    public Category(Tag tag)
    {
        this.tag = tag;
    }

    @Override
    public int hashCode()
    {
        return id;
    }

    public static void save(DBContext db, Category category)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(category);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static void delete(DBContext db, Category category)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(category);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static Category load(DBContext db, Integer id)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            return em.find(Category.class, id);
        } catch (PersistenceException e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static Category load(DBContext db, String name)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            String JPQL =
                "SELECT c FROM Category c JOIN c.tag t WHERE t.name=:name";
            TypedQuery<Category> q = em.createQuery(JPQL, Category.class);
            q.setParameter("name", name);
            return q.getSingleResultOrNull();
        } catch (PersistenceException e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static List<Category> loadAll(DBContext db)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Category> cq = cb.createQuery(Category.class);
            Root<Category> rootEntry = cq.from(Category.class);
            CriteriaQuery<Category> all = cq.select(rootEntry);
            TypedQuery<Category> allQuery = em.createQuery(all);
            return allQuery.getResultList();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }
}
