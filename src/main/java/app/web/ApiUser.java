package app.web;

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
            user = Account.read(db, username);
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

            Quiz.save(db, quiz);
        } catch (JsonException e) {
            throw new APIException(400, e);
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }

    static void putQuiz(DBContext db, Account user, String json)
            throws APIException
    {
        try {
            Quiz quiz;

            quiz = fromJson(json);
            quiz.owner = user;

            Quiz.update(db, quiz);
        } catch (JsonException e) {
            throw new APIException(400, e);
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }

}
