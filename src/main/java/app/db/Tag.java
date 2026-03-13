package app.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

@Entity
public class Tag
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false, unique = true)
    public String name;

    public Tag() {}
    public Tag(String name)
    {
        this.name = name;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        Tag t;
        if (o == null || o.getClass() != Tag.class)
            return false;
        t = (Tag)o;
        return t.id == id || t.name.equals(this.name);
    }

    public static void create(DBContext db, Tag tag)
        throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(tag);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            Tag tmp = Tag.loadByName(db, tag.name);
            if (tmp == null)
                throw new DBException(e.getMessage());
            tag.id = tmp.id;
        } finally {
            em.close();
        }
    }

    public static Tag load(DBContext db, Integer id)
        throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            return em.find(Tag.class, id);
        } catch (PersistenceException e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static Tag loadByName(DBContext db, String name)
        throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            String JPQL = "SELECT t FROM Tag t WHERE t.name = :name";
            TypedQuery<Tag> q = em.createQuery(JPQL, Tag.class);
            q.setParameter("name", name);
            return q.getSingleResult();
        } catch (PersistenceException e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static void gc(DBContext db)
        throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            em.getTransaction().begin();
            String SQL =
                "DELETE FROM tag WHERE NOT EXISTS "+
                "(SELECT quiz_tag.tags_id FROM quiz_tag "+
                "WHERE quiz_tag.tags_id=tag.id)";
            Query q = em.createNativeQuery(SQL);
            q.executeUpdate();
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }
}
