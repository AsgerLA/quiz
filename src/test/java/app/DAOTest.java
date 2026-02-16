package app;

import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DAOTest
{

    private static EntityManagerFactory emf;

    @BeforeAll
    static void beforeAll()
    {
        emf = HibernateConfig.createEntityManagerFactory();

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

    /**
     * Create new quiz as user.
     * Add new tag for quiz
     * Load the quiz and questions
     */
    @Test
    void createQuiz()
    {
        final int NUM_QUESTIONS = 10;
        User user = TestData.users[0];

        // add new quiz to DB
        Quiz quiz = new Quiz("Test Quiz", "Quiz used for testing");
        quiz.createdBy = user;

        Tag tag = new Tag("test");
        quiz.addTag(tag);
        quiz.addTag(tag);

        Question[] expected = new Question[NUM_QUESTIONS];

        for (int questionIndex = 0;
             questionIndex < NUM_QUESTIONS;
             questionIndex++) {
            expected[questionIndex] = new Question("question "+questionIndex, null);

            Question q = expected[questionIndex];
            q.addAnswer(new QuestionAnswer("answer1", 1));
            q.addAnswer(new QuestionAnswer("answer2", 0));
            quiz.addQuestion(q);
        }

        DAO.saveQuiz(emf, quiz);
        assertNotNull(quiz.id);

        // load quiz from DB
        // NOTE: the load order is not the same as save
        quiz = DAO.load(emf, Quiz.class, quiz.id);
        assertNotNull(quiz);
        Question[] actual = DAO.loadQuestions(emf, quiz.id).toArray(new Question[0]);
        assertEquals(expected.length, actual.length);
    }

    @Test
    void quizzes()
    {
        final int PAGE_SIZE = TestData.NUM_QUIZZES/2;
        int pageNum = 0;

        List<Quiz> quizzes = DAO.loadQuizzes(emf, pageNum, PAGE_SIZE);
        assertNotNull(quizzes);
        assertEquals(PAGE_SIZE, quizzes.size());
    }

}