package app.db;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.ArrayList;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

@Entity
public class Quiz
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public String description;

    public Instant created;
    public Instant modified;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    public Account owner;

    public boolean hidden;

    public int playCount;

    public int vote;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    public Set<Question> questions = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    public Set<Tag> tags = new HashSet<>();

    public Quiz() {}
    public Quiz(String title, String description)
    {
        this.title = title;
        this.description = description;
    }

    @PrePersist
    void prePersist()
    {
        created = Instant.now();
        modified = created;
    }

    @PreUpdate
    void preUpdate()
    {
        modified = Instant.now();
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
            em.merge(quiz);
            em.getTransaction().commit();
        } catch (Exception e) {
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
            String JPQL =
                "UPDATE Quiz q SET q.playCount = q.playCount + 1 WHERE q.id=:id";
            Query q = em.createQuery(JPQL);
            q.setParameter("id", id);
            em.getTransaction().begin();
            q.executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static void updateVote(DBContext db, boolean down, Integer id)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            int inc = down ? -1 : 1;
            String JPQL =
                "UPDATE Quiz q SET q.vote = q.vote + "+inc+" WHERE q.id=:id";
            Query q = em.createQuery(JPQL);
            q.setParameter("id", id);
            em.getTransaction().begin();
            q.executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static boolean delete(DBContext db, Account owner, Integer id)
        throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            Quiz quiz;
            quiz = em.find(Quiz.class, id);
            if (quiz == null)
                return false;
            if (owner != null && quiz.owner.id != owner.id)
                return false;
            em.getTransaction().begin();
            em.remove(quiz);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
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

    private static final int TOP_COUNT = 8;
    public static List<Quiz> loadTopByTag(DBContext db, String tagName)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            String JPQL =
                "SELECT q FROM Quiz q JOIN q.tags t WHERE t.name=:name";
            TypedQuery<Quiz> q = em.createQuery(JPQL, Quiz.class);
            q.setMaxResults(TOP_COUNT);
            q.setParameter("name", tagName);
            return q.getResultList();
        } catch (Exception e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    static List<Quiz> loadTopByAttribute(DBContext db, String attrName)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            String JPQL =
                "SELECT q FROM Quiz q ORDER BY q."+attrName+" ASC";
            TypedQuery<Quiz> tq = em.createQuery(JPQL, Quiz.class);
            tq.setMaxResults(TOP_COUNT);
            return tq.getResultList();
        } catch (Exception e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    static List<Quiz> loadTopByAttributeWithTag(DBContext db,
                                                String tagName,
                                                String attrName)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            String JPQL =
                "SELECT q FROM Quiz q JOIN q.tags t WHERE t.name=:name ORDER BY q."+attrName+" ASC";
            TypedQuery<Quiz> tq = em.createQuery(JPQL, Quiz.class);
            tq.setParameter("name", tagName);
            tq.setMaxResults(TOP_COUNT);
            return tq.getResultList();
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
                throws InvalidParameterException
        {
            try {
                pageSize = Integer.parseInt(getQueryParam("pages", "10", query));
                page = Integer.parseInt(getQueryParam("page", "1", query));
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e);
            }
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
                  sort.equals("vote"))) {
                throw new InvalidParameterException("sort");
            }
            if (!(order.equals("desc") ||
                  order.equals("asc"))) {
                throw new InvalidParameterException("order");
            }
            StringBuilder sb = new StringBuilder();

            sb.append("SELECT quiz.id FROM quiz");
            if (tag != null) {
                Tag t = Tag.loadByName(db, tag);
                if (t != null) {
                    sb.append(
        " JOIN quiz_tag ON quiz_tag.quiz_id=quiz.id WHERE quiz_tag.tags_id=");
                    sb.append(t.id);
                }
            }
            if (category != null) {
                Tag t = Tag.loadByName(db, category);
                if (t != null) {
                    if (tag == null)
                        sb.append(" WHERE");
                    else
                        sb.append(" AND");
                    sb.append(
    " EXISTS (SELECT category.id FROM category");
                    sb.append(
    " JOIN quiz_tag ON quiz_tag.quiz_id=quiz.id AND quiz_tag.tags_id=");
                    sb.append(t.id);
                    sb.append(" WHERE category.tag_id=");
                    sb.append(t.id);
                    sb.append(')');
                }
            }
            sb.append(" ORDER BY ");
            sb.append(sort);
            sb.append(' ');
            sb.append(order);
            Query q = em.createNativeQuery(sb.toString());
            if (pageSize > PAGE_SIZE)
                pageSize = PAGE_SIZE;
            q.setFirstResult(page * pageSize);
            q.setMaxResults(pageSize);
            List<Object> objects = q.getResultList();
            List<Quiz> results = new ArrayList<>(objects.size());
            Quiz quiz;
            for (Object o : objects) {
                quiz = new Quiz();
                quiz.id = (Integer)o;
                quiz = Quiz.load(db, quiz.id);
                results.add(quiz);
            }
            return results;
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e);
        } catch (InvalidParameterException e) {
            throw e;
        } catch (Exception e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    public static List<Quiz> loadBySearch(DBContext db, String search, int page)
            throws DBException
    {
        if (search == null)
            return null;
        if (--page < 0)
            page = 0;
        EntityManager em = db.emf.createEntityManager();
        try {
            String JPQL =
                "SELECT q FROM Quiz q JOIN q.tags t WHERE q.title LIKE :search1 OR t.name LIKE :search2";
            TypedQuery<Quiz> q = em.createQuery(JPQL, Quiz.class);
            search = "%" + search + "%";
            q.setParameter("search1", search);
            q.setParameter("search2", search);
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
