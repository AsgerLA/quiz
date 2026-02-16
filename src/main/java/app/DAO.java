package app;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

class DAO
{
    static void save(EntityManagerFactory emf,
                     Object o)
    {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(o);
            em.getTransaction().commit();
        }
    }

    static <T> T load(EntityManagerFactory emf,
                      Class<T> clazz, Long id)
    {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(clazz, id);
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

    static boolean signup(EntityManagerFactory emf,
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

    static User signin(EntityManagerFactory emf,
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

    static void saveQuiz(EntityManagerFactory emf,
                         Quiz quiz)
    {
        EntityManager em = emf.createEntityManager();
        try {
            //em.getTransaction().begin();
            for (QuizTag qt : quiz.quiztags) {
                TypedQuery<Tag> q = em.createQuery("SELECT t FROM Tag t WHERE t.name = :name", Tag.class);
                q.setParameter("name", qt.tag.name);
                Tag t = q.getSingleResultOrNull();
                if (t == null)
                    save(emf, qt.tag);
                else
                    qt.tag = t;
                //em.persist(qt);
            }
            //em.getTransaction().commit();

            em.getTransaction().begin();
            em.persist(quiz);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    static List<Quiz> loadQuizzes(EntityManagerFactory emf,
                                  int pageNum, int pageSize)
    {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Quiz> tq = em.createQuery("SELECT q FROM Quiz q", Quiz.class);
            tq.setFirstResult(pageNum*pageSize);
            tq.setMaxResults(pageSize);
            return tq.getResultList();
        }
    }

    static List<Question> loadQuestions(EntityManagerFactory emf,
                                        Long quizId)
    {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Question> tq = em.createQuery("SELECT qq.question FROM QuizQuestion qq WHERE qq.quiz.id = :quizId", Question.class);
            tq.setParameter("quizId", quizId);
            return tq.getResultList();
        }
    }

    static List<QuizQuestion> loadQuizQuestions(EntityManagerFactory emf,
                                                Long quizId)
    {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<QuizQuestion> tq = em.createQuery("SELECT qq FROM QuizQuestion qq WHERE qq.quiz.id = :quizId", QuizQuestion.class);
            tq.setParameter("quizId", quizId);
            return tq.getResultList();
        }
    }

}
