package app;

import app.persistence.*;
import jakarta.persistence.EntityManagerFactory;

import java.util.Random;

class TestData
{
    static final int NUM_QUIZZES = 5;
    static final int MIN_QUESTIONS = 2;
    static final int MAX_QUESTIONS = 10;

    static User[] users = new User[1];
    static Quiz[] quizzes = new Quiz[NUM_QUIZZES];
    static Tag[] tags = new Tag[1];

    static void populate(EntityManagerFactory emf)
    {
        Random rand = new Random();

        tags[0] = new Tag("test");
        DAO.save(emf, tags[0]);

        for (int quizIndex = 0;
             quizIndex < NUM_QUIZZES;
             quizIndex++) {
            quizzes[quizIndex] = new Quiz("Test Quiz", "Quiz used for testing");
            Quiz quiz = quizzes[quizIndex];
            quiz.addTag(tags[0]);

            int numQuestions = rand.nextInt(MIN_QUESTIONS, MAX_QUESTIONS);
            for (int questionIndex = 0;
                questionIndex < numQuestions;
                questionIndex++) {
                Question q;
                q = new Question("question " + questionIndex, null);
                q.addAnswer(new QuestionAnswer("answer1", 1));
                q.addAnswer(new QuestionAnswer("answer2", 0));

                quiz.addQuestion(q);
            }

            DAO.save(emf, quiz);
        }

        DAO.signup(emf, "testuser", "testpassword");
        User user1 = DAO.signin(emf, "testuser", "testpassword");
        if (user1 == null)
            throw new ExceptionInInitializerError("failed to create test user");
        users[0] = user1;
    }
}
