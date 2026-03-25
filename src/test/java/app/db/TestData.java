package app.db;

import java.util.Random;

public class TestData
{
    static final int NUM_QUIZZES = 80;
    static final int MIN_QUESTIONS = 2;
    static final int MAX_QUESTIONS = 10;

    public static Quiz[] quizzes = new Quiz[NUM_QUIZZES];

    public static void populate(DBContext db)
            throws DBException
    {
        Random rand = new Random();

        Tag[] cattags = new Tag[] {
            new Tag("geography"),
            new Tag("sports"),
            new Tag("science"),
            new Tag("history"),
            new Tag("trivia"),
        };

        Tag[] tags = new Tag[] {
            new Tag("test"),
            new Tag("quiz"),
            new Tag("usertag"),
        };

        for (Tag tag : cattags) {
            Tag.create(db, tag);
            Category.create(db, new Category(tag));
        }
        for (Tag tag : tags) {
            Tag.create(db, tag);
        }

        Account account = new Account("Test_user", "password");
        Account.create(db, account);

        for (int quizIndex = 0;
             quizIndex < NUM_QUIZZES;
             quizIndex++) {
            quizzes[quizIndex] = new Quiz("Test Quiz", "A quiz for testing");
            Quiz quiz = quizzes[quizIndex];
            quiz.owner = account;

            int numQuestions = rand.nextInt(MIN_QUESTIONS, MAX_QUESTIONS);
            for (int questionIndex = 0;
                 questionIndex < numQuestions;
                 questionIndex++) {
                Question q;
                q = new Question(quiz, "question " + questionIndex,
                                 questionIndex, Question.Type.SINGLE);
                q.answers.add(new Answer(q, "answer1", 1, 0));
                q.answers.add(new Answer(q, "answer2", 0, 1));

                quiz.questions.add(q);
            }

            quiz.tags.add(cattags[rand.nextInt(0, cattags.length)]);
            quiz.tags.add(tags[rand.nextInt(0, tags.length)]);
            Quiz.create(db, quiz);
        }
    }
}
