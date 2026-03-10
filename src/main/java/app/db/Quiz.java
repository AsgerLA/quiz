package app.db;

import java.security.InvalidParameterException;
import java.time.Instant;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Entity
public class Quiz
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false)
    public String title;

    public Instant created;
    public Instant modified;

    public int playCount;

    public int voteCount;
    public float voteAverage;

    public float rating;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    public Set<Question> questions = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    public Set<Tag> tags = new HashSet<>();

    public Quiz() {}
    public Quiz(String title)
    {
        this.title = title;
    }

    @PrePersist
    void prePersist()
    {
        created = Instant.now();
        modified = created;
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
            quiz.modified = Instant.now();
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
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    private static final int PAGE_SIZE = 20;
    public static List<Quiz> loadByQuery(DBContext db,
                                         int page,
                                         String sortKey,
                                         String sortOrder,
                                         String tag,
                                         String category)
            throws DBException, InvalidParameterException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            if (!(sortKey.equals("id") ||
                  sortKey.equals("created") ||
                  sortKey.equals("playCount") ||
                  sortKey.equals("rating"))) {
                throw new InvalidParameterException("sortKey");
            }
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Quiz> cq = cb.createQuery(Quiz.class);
            Root<Quiz> root = cq.from(Quiz.class);
            Join<Quiz, Tag> join = root.join("tags");

            Subquery<Category> sub = cq.subquery(Category.class);
            Root<Category> subroot = sub.from(Category.class);

            cq.select(root);
            if (tag != null && category != null) {
                Predicate p1 = null;
                Predicate p2 = null;
                Join<Category, Tag> join2 = subroot.join("tag");

                sub.where(cb.equal(join2.get("name"), category));

                p1 = cb.equal(join.get("name"), tag);
                p2 = cb.exists(sub);

                cq.where(cb.and(p1, p2));
            } else if (tag != null) {
                cq.where(cb.equal(join.get("name"), tag));
            } else if (category != null) {
                cq.where(cb.equal(join.get("name"), category));
            }

            if (sortOrder.equals("desc")) {
                cq.orderBy(cb.desc(root.get(sortKey)));
            } else if (sortOrder.equals("asc")) {
                cq.orderBy(cb.asc(root.get(sortKey)));
            } else {
                throw new InvalidParameterException("sortOrder");
            }

            TypedQuery<Quiz> q = em.createQuery(cq);
            q.setFirstResult(page*PAGE_SIZE);
            q.setMaxResults(PAGE_SIZE);
            return q.getResultList();
        } catch (PersistenceException e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }
}
