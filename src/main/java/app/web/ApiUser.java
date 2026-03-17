package app.web;

import java.util.List;

import app.db.Account;
import app.db.Answer;
import app.db.DBContext;
import app.db.DBException;
import app.db.Question;
import app.db.Quiz;
import app.db.Tag;
import app.web.json.JsonArray;
import app.web.json.JsonBuilder;
import app.web.json.JsonException;
import app.web.json.JsonObject;
import app.web.json.JsonParser;

class ApiUser
{
    static void post(DBContext db, String json)
            throws APIException
    {
        JsonObject jo;
        Account account;
        String username;

        try {
            jo = JsonParser.decodeObject(json);

            username = jo.getString("username");
            if (!Account.verifyUsername(username))
                throw new APIException(400, "bad username");

            account = new Account(username);

            Account.create(db, account);
        } catch (JsonException e) {
            throw new APIException(400, e);
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }

    static String get(DBContext db, String username)
            throws APIException
    {
        JsonBuilder jb;
        Account user;

        try {
            user = Account.loadByName(db, username);
        } catch (DBException e) {
            throw new APIException(500, e);
        }

        if (user == null)
            throw new APIException(404, "account not found");

        jb = new JsonBuilder();

        jb.objectBegin();
        jb.field("id", user.id);
        jb.field("username", user.username);
        jb.objectEnd();

        return jb.build();
    }

    private static void addQuestion(JsonBuilder jb, Question question)
    {
        jb.objectBegin();
            jb.field("id", question.id);
            jb.field("question", question.question);
            jb.field("slot", question.question);
            jb.arrayBegin("answers");
            for (Answer answer : question.answers) {
                jb.objectBegin();
                    jb.field("id", answer.id);
                    jb.field("answer", answer.answer);
                    jb.field("points", answer.points);
                    jb.field("slot", answer.slot);
                jb.objectEnd();
            }
            jb.arrayEnd();
        jb.objectEnd();
    }

    static String getQuiz(DBContext db, Account user, Integer id)
            throws APIException
    {
        JsonBuilder jb;
        Quiz quiz;
        List<Question> questions;

        try {
            quiz = Quiz.load(db, id);
            if (quiz.owner.id != user.id)
                throw new APIException(403, "not owner");
            questions = Question.loadByQuizId(db, quiz.id);
        } catch (DBException e) {
            throw new APIException(500, e);
        }

        jb = new JsonBuilder();
        jb.objectBegin();
            jb.field("quiz", (Object)ApiQuiz.toJson(quiz));
            jb.arrayBegin("questions");
            for (Question question : questions)
                addQuestion(jb, question);
            jb.arrayEnd();
        jb.objectEnd();

        return jb.build();
    }

    private static Quiz fromJson(String json)
        throws JsonException
    {
        JsonObject jo;
        JsonArray ja;
        Object id;
        Quiz quiz;
        int i;

        jo = JsonParser.decodeObject(json);

        quiz = new Quiz(jo.getString("title"));
        id = jo.get("id");
        if (id instanceof Number)
            quiz.id = ((Number)id).intValue();

        ja = jo.getJsonArray("tags");
        for (i = 0; i < ja.size(); i++) {
            Tag tag = new Tag(ja.getString(i));
            quiz.tags.add(tag);
        }

        ja = jo.getJsonArray("questions");
        for (i = 0; i < ja.size(); i++) {
            JsonObject tmp = ja.getJsonObject(i);
            Question question = new Question(
                    quiz,
                    tmp.getString("question"),
                    tmp.getInt("slot"));
            id = jo.get("id");
            if (id instanceof Number)
                question.id = ((Number)id).intValue();
            JsonArray answerJA = tmp.getJsonArray("answers");
            for (int j = 0; j < answerJA.size(); j++) {
                JsonObject answerJO = answerJA.getJsonObject(i);
                Answer answer = new Answer(
                        question,
                        answerJO.getString("answer"),
                        answerJO.getInt("points"),
                        answerJO.getInt("slot"));
                question.answers.add(answer);
            }
            quiz.questions.add(question);
        }

        return quiz;
    }

    static void postQuiz(DBContext db, Account user, String json)
            throws APIException
    {
        try {
            Quiz quiz;

            quiz = fromJson(json);
            quiz.owner = user;

            Quiz.create(db, quiz);
            new ThreadCacheWriter(db).start();
        } catch (JsonException e) {
            throw new APIException(400, e);
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }

    private static enum Action
    {
        CREATE,
        UPDATE,
        DELETE,
    }

    private static void _putTags(DBContext db, Quiz quiz, JsonArray tagsJA)
            throws APIException, DBException, JsonException
    {
        JsonObject tagJO;
        Action action;

        for (int i = 0; i < tagsJA.size(); i++) {
            tagJO = tagsJA.getJsonObject(i);
            action = Action.valueOf(tagJO.getString("action").toUpperCase());
            if (action == null)
                throw new APIException(400, "bad action");

            switch (action) {
                case Action.DELETE:
                    Quiz.deleteTag(db, quiz.id, tagJO.getInt("id"));
                    break;
                case Action.CREATE:
                    quiz.tags.add(new Tag(tagJO.getString("name")));
                    break;
                default:
                    throw new APIException(400, "bad action");
            }
        }
    }

    private static void _putQuestions(DBContext db,
                                      Quiz quiz,
                                      JsonArray questionsJA)
            throws APIException, DBException, JsonException
    {
        JsonObject questionJO;
        JsonArray answersJA;
        Question question;
        Action action;

        if (questionsJA.size() == 0)
            throw new APIException(400, "no questions");

        for (int i = 0; i < questionsJA.size(); i++) {
            questionJO = questionsJA.getJsonObject(i);
            action = Action.valueOf(questionJO.getString("action").toUpperCase());

            switch (action) {
                case Action.DELETE:
                    Question.delete(db, questionJO.getInt("id"));
                    break;
                case Action.CREATE:
                    question =
                        new Question(quiz,
                                     questionJO.getString("question"),
                                     questionJO.getInt("slot"));
                    answersJA = questionJO.getJsonArray("answers");
                    Question.create(db, question);
                    _putAnswers(db, question, answersJA);
                    break;
                case Action.UPDATE:
                    question = Question.load(db, questionJO.getInt("id"));
                    question.question = questionJO.getString("question");
                    question.slot = questionJO.getInt("slot");
                    Question.update(db, question);
                    answersJA = questionJO.getJsonArray("answers");
                    _putAnswers(db, question, answersJA);
                    break;
                default:
                    throw new APIException(400, "bad action");
            }
        }
    }

    private static void _putAnswers(DBContext db,
                                    Question question,
                                    JsonArray answersJA)
            throws APIException, DBException, JsonException
    {
        JsonObject answerJO;
        Answer answer;
        Action action;

        if (answersJA.size() == 0)
            throw new APIException(400, "no questions");

        for (int i = 0; i < answersJA.size(); i++) {
            answerJO = answersJA.getJsonObject(i);
            action = Action.valueOf(answerJO.getString("action").toUpperCase());

            switch (action) {
                case Action.DELETE:
                    Answer.delete(db, answerJO.getInt("id"));
                    break;
                case Action.CREATE:
                    answer =
                        new Answer(question,
                                   answerJO.getString("answer"),
                                   answerJO.getInt("points"),
                                   answerJO.getInt("slot"));
                    Answer.create(db, answer);
                    break;
                case Action.UPDATE:
                    answer = Answer.load(db, answerJO.getInt("id"));
                    answer.answer = answerJO.getString("answer");
                    answer.slot = answerJO.getInt("slot");
                    answer.points = answerJO.getInt("points");
                    Answer.update(db, answer);
                    break;
                default:
                    throw new APIException(400, "bad action");
            }
        }
    }

    static void putQuiz(DBContext db, Account user, String json)
            throws APIException
    {
        try {
            Quiz quiz = new Quiz();
            JsonObject quizJO;
            JsonArray ja;

            quizJO = JsonParser.decodeObject(json);

            quiz.id = quizJO.getInt("id");
            quiz = Quiz.load(db, quiz.id);
            if (quiz == null)
                throw new APIException(404, "quiz not found");
            if (quiz.owner.id != user.id)
                throw new APIException(403, "not owner");

            quiz.title = quizJO.getString("title");

            ja = quizJO.getJsonArray("tags");
            _putTags(db, quiz, ja);

            ja = quizJO.getJsonArray("questions");
            _putQuestions(db, quiz, ja);

            Quiz.update(db, quiz);
            Tag.gc(db);

            new ThreadCacheWriter(db).start();
        } catch (JsonException e) {
            throw new APIException(400, e);
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }

    static void deleteQuiz(DBContext db, Account user, Integer id)
            throws APIException
    {
        try {
            Quiz.delete(db, user, id);

            new ThreadCacheWriter(db).start();
        } catch (IllegalArgumentException e) {
            throw new APIException(403, e);
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }
}
