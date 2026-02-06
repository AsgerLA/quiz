package app;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DAOTest
{

    private static EntityManagerFactory emf;

    @BeforeAll
    static void beforeAll()
    {
        emf = HibernateConfig.createEntityManagerFactory("hibernate-test.properties");
    }

    @AfterAll
    static void afterAll()
    {
        if (emf != null)
            emf.close();
    }

    @Test
    void signup_signin()
    {
        String username = "testUser";
        String password = "password1234";

        assertTrue(DAO.signup(emf, username, password));
        assertNotNull(DAO.signin(emf, username, password));
    }
}