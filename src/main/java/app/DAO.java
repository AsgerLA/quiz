package app;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.time.Instant;
import java.util.Arrays;

class DAO
{

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
            throws Exception
    {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, PASSWORD_HASH_LEN);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
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
            throw new APIException(500, e.getMessage());
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
            throw new APIException(500, e.getMessage());
        }
    }
}
