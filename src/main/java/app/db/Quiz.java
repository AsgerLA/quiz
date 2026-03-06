package app.db;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@Entity
public class Quiz
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false)
    public String title;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    public Set<Question> questions = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    public Set<Tag> tags = new HashSet<>();

    public Quiz() {}

    public Quiz(String title)
    {
        this.title = title;
    }

    public static void save(DBContext db, Quiz quiz)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            for (Tag tag : quiz.tags)
                Tag.save(db, tag);
            em.getTransaction().begin();
            em.persist(quiz);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static void update(DBContext db, Quiz quiz)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            for (Tag tag : quiz.tags)
                Tag.save(db, tag);
            em.getTransaction().begin();
            em.merge(quiz);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static Quiz load(DBContext db, int id)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            return em.find(Quiz.class, id);
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static List<Quiz> loadAll(DBContext db)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Quiz> cq = cb.createQuery(Quiz.class);
            Root<Quiz> rootEntry = cq.from(Quiz.class);
            CriteriaQuery<Quiz> all = cq.select(rootEntry);
            TypedQuery<Quiz> allQuery = em.createQuery(all);
            return allQuery.getResultList();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static List<Quiz> loadTop10ByTag(DBContext db, String name)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            String JPQL = "SELECT q FROM Quiz q JOIN q.tags t WHERE t.name = :name";
            TypedQuery<Quiz> q = em.createQuery(JPQL, Quiz.class);
            q.setParameter("name", name);
            q.setMaxResults(10);
            return q.getResultList();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }
}
