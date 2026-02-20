package app.service;

public interface IService
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
