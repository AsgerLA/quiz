package app.persistence;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.exception.ConstraintViolationException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.*;

public class DAO
{
    public static void save(EntityManagerFactory emf,
                            Object o)
    {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(o);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public static <T> T load(EntityManagerFactory emf,
                             Class<T> clazz, Object id)
    {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(clazz, id);
        }
    }

    public static <T> List<T> loadAll(EntityManagerFactory emf,
                                      Class<T> clazz)
    {
        try (EntityManager em = emf.createEntityManager()) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(clazz);
            Root<T> rootEntry = cq.from(clazz);
            CriteriaQuery<T> all = cq.select(rootEntry);
            TypedQuery<T> allQuery = em.createQuery(all);
            return allQuery.getResultList();
        }
    }

    public static <T> List<T> loadPage(EntityManagerFactory emf,
                                       Class<T> clazz, int pageNum, int pageSize)
    {
        try (EntityManager em = emf.createEntityManager()) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(clazz);
            Root<T> rootEntry = cq.from(clazz);
            CriteriaQuery<T> all = cq.select(rootEntry);
            TypedQuery<T> tq = em.createQuery(all);
            tq.setFirstResult(pageNum*pageSize);
            tq.setMaxResults(pageSize);
            return tq.getResultList();
        }
    }

    public static <T> T findOrCreate(EntityManagerFactory emf,
                              Class<T> clazz,
                              Object key, T data)
    {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            em.persist(data);
            em.getTransaction().commit();
            return data;
        } catch (ConstraintViolationException e) {
            T ent = em.find(clazz, key);
            if (ent == null)
                throw e;
            return ent;
        } catch (RuntimeException e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    private static final int PASSWORD_SALT_LEN = 16;
    private static final int PASSWORD_HASH_LEN = 128;
    private static byte[] genSalt()
    {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[PASSWORD_SALT_LEN];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] hashPassword(String password, byte[] salt)
            throws GeneralSecurityException
    {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, PASSWORD_HASH_LEN);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new GeneralSecurityException(e);
        }
    }

    public static boolean signup(EntityManagerFactory emf,
                          String username, String password)
    {
        try (EntityManager em = emf.createEntityManager()) {
            User user;
            TypedQuery<User> q;

            q = em.createQuery("SELECT u FROM User u WHERE u.name = :username", User.class);
            q.setParameter("username", username);
            user = q.getSingleResultOrNull();
            if (user != null)
                return false;
            user = new User();

            user.name = username;
            user.salt = genSalt();
            user.password = hashPassword(password, user.salt);

            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static User signin(EntityManagerFactory emf,
                       String username, String password)
    {
        try (EntityManager em = emf.createEntityManager()) {
            User user;
            TypedQuery<User> q;
            byte[] hash;

            q = em.createQuery("SELECT u FROM User u WHERE u.name = :username", User.class);
            q.setParameter("username", username);
            user = q.getSingleResultOrNull();
            if (user == null)
                return null;
            hash = hashPassword(password, user.salt);
            if (!Arrays.equals(hash, user.password))
                return null;
            user.lastLogin = Instant.now();

            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Question> loadQuestions(EntityManager em, Long quizId)
    {
        TypedQuery<Question> tq = em.createQuery("SELECT q FROM Question q WHERE q.quiz.id = :quizId", Question.class);
        tq.setParameter("quizId", quizId);
        return tq.getResultList();
    }

    private static List<QuizTag> loadQuizTags(EntityManager em, Long quizId)
    {
        TypedQuery<QuizTag> tq = em.createQuery("SELECT qt FROM QuizTag qt WHERE qt.quiz.id = :quizId", QuizTag.class);
        tq.setParameter("quizId", quizId);
        return tq.getResultList();
    }

    /**
     * Load entire quiz, including other tables
     */
    public static Quiz loadQuiz(EntityManagerFactory emf,
                                Long quizId) {
        try (EntityManager em = emf.createEntityManager()) {
            Quiz quiz = em.find(Quiz.class, quizId);
            quiz.questions = new HashSet<>(loadQuestions(em, quizId));
            quiz.quiztags = new HashSet<>(loadQuizTags(em, quizId));
            return quiz;
        }
    }

    public static QuestionCategory findQuestionCategory(EntityManagerFactory emf,
                                                 String name)
    {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<QuestionCategory> tq = em.createQuery("SELECT qc FROM QuestionCategory qc WHERE qc.name = :name", QuestionCategory.class);
            tq.setParameter("name", name);
            return tq.getSingleResultOrNull();
        }
    }

}
