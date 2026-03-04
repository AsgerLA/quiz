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

    @ManyToOne(fetch = FetchType.LAZY)
    public Category category;

    @OneToMany(mappedBy = "question", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Set<Answer> answers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    Quiz quiz;

    public Question() {}
    public Question(Quiz quiz, String question, Category category, int slot)
    {
        this.quiz = quiz;
        this.question = question;
        this.category = category;
        this.slot = slot;
    }

    @Override
    public int hashCode()
    {
        return slot;
    }

    public static List<Question> loadByQuizId(DBContext ctx, int quizId)
            throws DBException
    {
        EntityManager em = ctx.emf.createEntityManager();
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
