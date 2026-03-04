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

    @Column(nullable = false)
    public String name;

    public Category() {}
    public Category(String name)
    {
        this.name = name;
    }

    @Override
    public int hashCode()
    {
        return id;
    }

    private static final Set<Category> cache = Collections.synchronizedSet(new HashSet<>());

    public static void save(DBContext ctx, Category category)
            throws DBException
    {
        if (cache.isEmpty())
            cache.addAll(loadAll(ctx));
        EntityManager em = ctx.emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(category);
            em.getTransaction().commit();
            cache.add(category);
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static void delete(DBContext ctx, Category category)
            throws DBException
    {
        if (cache.isEmpty()) {
            cache.addAll(loadAll(ctx));
        }
        EntityManager em = ctx.emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(category);
            em.getTransaction().commit();
            cache.remove(category);
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static Category load(DBContext ctx, int id)
            throws DBException
    {
        if (cache.isEmpty())
            cache.addAll(loadAll(ctx));
        Iterator<Category> it = cache.iterator();
        Category cat;
        while (it.hasNext()) {
            cat = it.next();
            if (cat.id == id)
                return cat;
        }
        return null;
    }

    public static Set<Category> loadAll(DBContext ctx)
            throws DBException
    {
        if (!cache.isEmpty()) {
            return cache;
        }
        EntityManager em = ctx.emf.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Category> cq = cb.createQuery(Category.class);
            Root<Category> rootEntry = cq.from(Category.class);
            CriteriaQuery<Category> all = cq.select(rootEntry);
            TypedQuery<Category> allQuery = em.createQuery(all);
            cache.addAll(allQuery.getResultList());
            return cache;
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }
}
