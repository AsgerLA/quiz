package app.db;

import java.util.Random;

public class TestData
{
    static final int NUM_QUIZZES = 5;
    static final int MIN_QUESTIONS = 2;
    static final int MAX_QUESTIONS = 10;

    public static Quiz[] quizzes = new Quiz[NUM_QUIZZES];

    public static void populate(DBContext db)
            throws DBException
    {
        Random rand = new Random();

        Tag tag = new Tag("test");
        Tag.save(db, tag);

        Category category = new Category(tag);
        Category.save(db, category);

        for (int quizIndex = 0;
             quizIndex < NUM_QUIZZES;
             quizIndex++) {
            quizzes[quizIndex] = new Quiz("Test Quiz");
            Quiz quiz = quizzes[quizIndex];

            int numQuestions = rand.nextInt(MIN_QUESTIONS, MAX_QUESTIONS);
            for (int questionIndex = 0;
                 questionIndex < numQuestions;
                 questionIndex++) {
                Question q;
                q = new Question(quiz, "question " + questionIndex, null, questionIndex);
                q.answers.add(new Answer(q, "answer1", 1, 0));
                q.answers.add(new Answer(q, "answer2", 0, 1));

                quiz.questions.add(q);
            }

            quiz.tags.add(tag);
            Quiz.save(db, quiz);
        }
    }
}
