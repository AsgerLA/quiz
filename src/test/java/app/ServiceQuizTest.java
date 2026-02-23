package app;

import app.persistence.User;
import app.service.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ServiceTest.class)
class ServiceQuizTest
{

    @Test
    void createQuiz()
            throws IOException
    {
        User user = TestData.users[0];
        final String JSON =
                """
{
  "title" : "Test Quiz",
  "description" : "Quiz used for testing",
  "tags" : [ "test", "quiz" ],
  "questions" : [
    {
      "question" : "question",
      "category" : "test",
      "answers" : [
        {
          "answer" : "answer1",
          "points" : 1
        },
        {
          "answer" : "answer2",
          "points" : 0
        }
      ]
    }
  ]
}
                """;
        QuizDTO expected = Service.jsonToObject(JSON, QuizDTO.class);

        Long quizId = Service.quiz.createQuiz(user.id, expected);
        assertNotNull(quizId);
        expected.id = quizId;
        QuizDTO actual = Service.quiz.loadQuiz(quizId);
        assertEquals(expected, actual);
    }

    @Test
    void playQuiz()
    {
        // user opens home page
        PageDTO<QuizDTO> page = Service.quiz.loadQuizPage(0);
        assertNotEquals(0, page.results.length);

        // user picks a quiz
        Long quizId = page.results[0].id;

        // load quiz with questions into a session
        QuizDTO quiz = Service.quiz.loadQuiz(quizId);
        assertNotEquals(0, quiz.questions.length);
    }
}