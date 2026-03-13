package app.db;


import jakarta.persistence.*;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Question
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false)
    public String question;

    @Column(nullable = false)
    public int slot;

    @OneToMany(mappedBy = "question", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Set<Answer> answers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    Quiz quiz;

    public Question() {}
    public Question(Quiz quiz, String question, int slot)
    {
        this.quiz = quiz;
        this.question = question;
        this.slot = slot;
    }

    @Override
    public int hashCode()
    {
        return slot;
    }

    public static void create(DBContext db, Question question)
        throws DBException
    {
        CRUD.create(db, question);
    }

    public static void update(DBContext db, Question question)
        throws DBException
    {
        CRUD.update(db, question);
    }

    public static void delete(DBContext db, Integer id)
        throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            String SQL =
                "DELETE FROM Answer WHERE answer.question_id=:id1; DELETE FROM Question WHERE question.id=:id2";
            Query q = em.createNativeQuery(SQL);
            q.setParameter("id1", id);
            q.setParameter("id2", id);
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

    public static Question load(DBContext db, Integer id)
        throws DBException
    {
        return CRUD.read(db, Question.class, id);
    }

    public static List<Question> loadByQuizId(DBContext db, int quizId)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            String JPQL = "SELECT q FROM Question q WHERE q.quiz.id = :quizId";
            TypedQuery<Question> q = em.createQuery(JPQL, Question.class);
            q.setParameter("quizId", quizId);
            return q.getResultList();
        } catch (PersistenceException e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }
}
