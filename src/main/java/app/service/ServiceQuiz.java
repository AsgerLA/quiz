package app.service;

import app.persistence.*;

import java.util.List;

public class ServiceQuiz
{
    public Long createQuiz(Long userId, QuizDTO quizDTO)
    {
        Quiz quiz = new Quiz();

        quiz.createdBy = DAO.load(Service.emf, User.class, userId);

        quiz.title = quizDTO.title;
        quiz.description = quizDTO.description;
        for (QuestionDTO questionDTO : quizDTO.questions) {
            QuestionCategory qc = DAO.findQuestionCategory(Service.emf, questionDTO.category);
            Question q = new Question(questionDTO.question, qc);
            quiz.addQuestion(q);
        }
        for (String tagName : quizDTO.tags) {
            Tag tag = new Tag(tagName);
            tag = DAO.findOrCreate(Service.emf, Tag.class, tagName, tag);
            quiz.addTag(tag);
        }

        DAO.save(Service.emf, quiz);

        return quiz.id;
    }

    public QuizDTO loadQuiz(Long quizId)
    {
        QuizDTO quizDTO;
        Quiz quiz;

        quiz = DAO.loadQuiz(Service.emf, quizId);
        assert !quiz.questions.isEmpty();

        // TODO: move this to mapper methods
        quizDTO = new QuizDTO();
        quizDTO.id = quiz.id;
        quizDTO.title = quiz.title;
        quizDTO.description = quiz.description;

        // TODO: TagDTO with id and name
        quizDTO.tags = new String[quiz.quiztags.size()];
        int tagIndex = 0;
        for (QuizTag qt : quiz.quiztags)
            quizDTO.tags[tagIndex++] = qt.tag.name;

        quizDTO.questions = new QuestionDTO[quiz.questions.size()];
        int questionIndex = 0;
        for (Question question : quiz.questions) {
            QuestionDTO questionDTO;
            quizDTO.questions[questionIndex] = new QuestionDTO();
            questionDTO = quizDTO.questions[questionIndex];
            questionIndex++;

            questionDTO.question = question.question;
            if (question.category != null)
                questionDTO.category = question.category.name;
            questionDTO.answers = new QuestionAnswerDTO[question.answers.size()];
            int answerIndex = 0;
            for (QuestionAnswer answer : question.answers) {
                questionDTO.answers[answerIndex] = new QuestionAnswerDTO();
                QuestionAnswerDTO answerDTO = questionDTO.answers[answerIndex];
                answerIndex++;

                answerDTO.answer = answer.answer;
                answerDTO.points = answer.points;
            }
        }
        return quizDTO;
    }

    /**
     * Convert database entity 'Quiz' to 'QuizDTO'.
     * Does not convert inner DTO's
     */
    private QuizDTO[] quizzesToDTO(List<Quiz> quizzes)
    {
        QuizDTO[] results = new QuizDTO[quizzes.size()];
        int i = 0;
        for (Quiz q : quizzes) {
            // TODO: finish
            results[i] = new QuizDTO();
            results[i].id = q.id;
            results[i].title = q.title;
            results[i].description = q.description;
        }
        return results;
    }

    private final int QUIZ_PAGE_SIZE = 4;
    public PageDTO<QuizDTO> loadQuizPage(int page)
    {
        PageDTO<QuizDTO> pageDTO;
        List<Quiz> quizzes;
        QuizDTO[] results;

        quizzes = DAO.loadPage(Service.emf, Quiz.class, page, QUIZ_PAGE_SIZE);
        results = quizzesToDTO(quizzes);
        pageDTO = new PageDTO<>();
        pageDTO.page = page;
        pageDTO.results = results;
        return pageDTO;
    }

}
