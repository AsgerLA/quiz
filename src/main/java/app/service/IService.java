package app.service;

public interface IService
{
    interface Quiz
    {
        /**
         * Create a new quiz
         */
        Long createQuiz(Long userId, QuizDTO quizDTO);

        /**
         * Load all data for a quiz.
         * NOTE: do NOT send answers
         * points to players.
         */
        QuizDTO loadQuiz(Long quizId);

        PageDTO<QuizDTO> loadQuizPage(int page);
    }

    interface User
    {
        boolean signup(String username, String password);

        UserDTO signin(String username, String password);
    }
}
