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

    public static void create(DBContext db, Category category)
            throws DBException
    {
        CRUD.create(db, category);
    }

    public static void delete(DBContext db, Category category)
            throws DBException
    {
        CRUD.delete(db, category);
    }

    public static Category load(DBContext db, Integer id)
            throws DBException
    {
        return CRUD.read(db, Category.class, id);
    }

    public static Category loadByName(DBContext db, String name)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            String JPQL = "SELECT c FROM Category c JOIN c.tag t WHERE t.name=:name";
            TypedQuery<Category> q = em.createQuery(JPQL, Category.class);
            q.setParameter("name", name);
            q.setMaxResults(1);
            return q.getSingleResultOrNull();
        } catch (Exception e) {
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
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static List<Tag> loadSubTags(DBContext db, Integer id)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            List<Object[]> results;
            List<Tag> tags;
            Tag tag;

            String SQL = """
                SELECT DISTINCT tag.id, tag.name FROM tag
                JOIN quiz_tag ON quiz_tag.tags_id=tag.id
                JOIN quiz ON quiz.id=quiz_tag.quiz_id
                WHERE EXISTS (
                        SELECT category.id FROM category
                        JOIN quiz_tag ON quiz_tag.quiz_id=quiz.id
                        WHERE quiz_tag.tags_id=:id
                        )
                """;
            Query q = em.createNativeQuery(SQL);
            q.setParameter("id", id);
            results = q.getResultList();
            tags = new ArrayList<>(results.size());
            for (Object[] o : results) {
                tag = new Tag();
                tag.id = (Integer)o[0];
                tag.name = (String)o[1];
                tags.add(tag);
            }
            return tags;
        } catch (Exception e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }
}
