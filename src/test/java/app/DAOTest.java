package app;

import app.persistence.DAO;
import app.persistence.User;
import app.service.*;
import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DAOTest
{

    private static EntityManagerFactory emf;

    @BeforeAll
    static void beforeAll()
    {
        emf = Service.getEntityManagerFactory();//HibernateConfig.createEntityManagerFactory();

        TestData.populate(emf);
    }

    @AfterAll
    static void afterAll()
    {
        // print hibernate metrics
        SessionFactory sf = emf.unwrap(SessionFactory.class);
        Statistics stats = sf.getStatistics();
        System.out.println(stats.toString());

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

    @Test
    void createQuiz()
            throws IOException
    {
        User user = TestData.users[0];
        final String JSON = Files.readString(Path.of("create_quiz.json"));
        QuizDTO expected = Service.jsonToObject(JSON, QuizDTO.class);

        IService service = Service.getService();
        Long quizId = service.createQuiz(user.id, expected);
        assertNotNull(quizId);
        expected.id = quizId;
        QuizDTO actual = service.loadQuiz(quizId);
        assertEquals(expected, actual);
    }

    @Test
    void playQuiz()
    {
        IService service = Service.getService();

        // user opens home page
        PageDTO<QuizDTO> page = service.loadQuizPage(0);

        // user picks a quiz
        Long quizId = page.results[0].id;

        // load quiz with questions into a session
        QuizDTO quiz = service.loadQuiz(quizId);
        assertNotEquals(0, quiz.questions.length);
    }
}