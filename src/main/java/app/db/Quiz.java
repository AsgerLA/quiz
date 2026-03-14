package app.db;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Query;
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

    @ManyToOne(fetch = FetchType.EAGER)
    public Account owner;

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

    public static void create(DBContext db, Quiz quiz)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            for (Tag tag : quiz.tags)
                Tag.create(db, tag);
            em.getTransaction().begin();
            em.persist(quiz);
            em.getTransaction().commit();
        } catch (Exception e) {
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
                Tag.create(db, tag);
            em.getTransaction().begin();
            quiz.modified = Instant.now();
            em.merge(quiz);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static void updatePlayCount(DBContext db, Integer id)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            String SQL =
                "UPDATE quiz SET playCount = playCount + 1 WHERE id=:id";
            Query q = em.createNativeQuery(SQL);
            q.setParameter("id", id);
            em.getTransaction().begin();
            q.executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static void delete(DBContext db, Account owner, Integer id)
        throws DBException, IllegalArgumentException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            Quiz quiz;
            quiz = em.find(Quiz.class, id);
            if (owner != null && quiz.owner.id != owner.id)
                throw new IllegalArgumentException("bad owner");
            em.getTransaction().begin();
            em.remove(quiz);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static void deleteTag(DBContext db, Integer quizId, Integer tagId)
        throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            int n;
            String SQL =
                "DELETE FROM quiz_tag WHERE quiz_id = :quizId AND tags_id = tagId";
            em.getTransaction().begin();
            Query q = em.createNativeQuery(SQL);
            q.setParameter("quizId", quizId);
            q.setParameter("tagId", tagId);
            n = q.executeUpdate();
            if (n != 1)
                throw new PersistenceException("deleteTag(): updated "+n+" entities!");
            em.getTransaction().commit();
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static class QueryParam
    {
        public int page;
        public int pageSize;
        public String sort;
        public String order;
        public String tag;
        public String category;

        public QueryParam()
        {
            page = 1;
            pageSize = 10;
            sort = "title";
            order = "asc";
            tag = null;
            category = null;
        }

        public QueryParam(Map<String, List<String>> query)
        {
            pageSize = 10;
            page = Integer.parseInt(getQueryParam("page", "1", query));
            sort = getQueryParam("sort", "title", query);
            order = getQueryParam("order", "asc", query);
            tag = getQueryParam("tag", null, query);
            category = getQueryParam("category", null, query);
            if (page < 1)
                throw new InvalidParameterException("page < 1");
        }

        private static String getQueryParam(String key, String value,
                Map<String, List<String>> query)
        {
            List<String> values;
            values = query.get(key);
            if (values == null || values.isEmpty())
                return value;
            return values.get(0);
        }
    }

    private static final int PAGE_SIZE = 20;
    public static List<Quiz> loadByQuery(DBContext db,
                                         QueryParam query,
                                         Integer ownerId)
            throws DBException, InvalidParameterException
    {
        int page, pageSize;
        String sort;
        String order;
        String tag;
        String category;
        EntityManager em = db.emf.createEntityManager();
        try {
            pageSize = query.pageSize;
            page = query.page - 1;
            sort = query.sort;
            order = query.order;
            tag = query.tag;
            category = query.category;
            if (page < 0)
                page = 0;
            if (!(sort.equals("title") ||
                  sort.equals("created") ||
                  sort.equals("playCount") ||
                  sort.equals("rating"))) {
                throw new InvalidParameterException("sort");
            }
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Quiz> cq = cb.createQuery(Quiz.class);
            Root<Quiz> root = cq.from(Quiz.class);
            Join<Quiz, Tag> join = root.join("tags");

            Subquery<Category> sub = cq.subquery(Category.class);
            Root<Category> subroot = sub.from(Category.class);

            cq.select(root);
            if (ownerId != null) {
                Join<Quiz, Account> joinAccount = root.join("owner");
                cq.where(cb.equal(joinAccount.get("id"), ownerId));
            }
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

            if (order.equals("desc")) {
                cq.orderBy(cb.desc(root.get(sort)));
            } else if (order.equals("asc")) {
                cq.orderBy(cb.asc(root.get(sort)));
            } else {
                throw new InvalidParameterException("order");
            }

            TypedQuery<Quiz> q = em.createQuery(cq);
            if (pageSize > PAGE_SIZE)
                pageSize = PAGE_SIZE;
            q.setFirstResult(page * pageSize);
            q.setMaxResults(pageSize);
            return q.getResultList();
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e);
        } catch (Exception e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static List<Quiz> loadBySearch(DBContext db, String search, int page)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            String JPQL = "SELECT q FROM Quiz q JOIN q.tags t WHERE q.title LIKE :search1 OR t.name LIKE :search2";
            TypedQuery<Quiz> q = em.createQuery(JPQL, Quiz.class);
            search = "%" + search + "%";
            q.setParameter("search1", search);
            q.setParameter("search2", search);
            if (--page < 0)
                page = 0;
            q.setFirstResult(page*PAGE_SIZE);
            q.setMaxResults(PAGE_SIZE);
            return q.getResultList();
        } catch (Exception e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }
}
