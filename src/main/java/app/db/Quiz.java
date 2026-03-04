package app.db;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Quiz
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false)
    public String title;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.PERSIST)
    public Set<Question> questions = new HashSet<>();

    public Quiz() {}
    public Quiz(String title)
    {
        this.title = title;
    }

    public static void save(DBContext ctx, Quiz quiz)
            throws DBException
    {
        EntityManager em = ctx.emf.createEntityManager();
        try {
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

    public static Quiz load(DBContext ctx, int id)
            throws DBException
    {
        EntityManager em = ctx.emf.createEntityManager();
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

    public static List<Quiz> loadAll(DBContext ctx)
            throws DBException
    {
        EntityManager em = ctx.emf.createEntityManager();
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
}
