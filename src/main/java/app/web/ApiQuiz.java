package app.web;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

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

class ApiQuiz
{
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

    static String toJson(Quiz quiz)
    {
        JsonBuilder jb = new JsonBuilder();

        jb.objectBegin();
        jb.field("id", quiz.id);
        jb.field("title", quiz.title);
        jb.field("playCount", quiz.playCount);
        jb.field("voteAverage", quiz.voteAverage);
        jb.field("created", quiz.created.toString());
        jb.arrayBegin("tags");
        for (Tag tag : quiz.tags)
            jb.value(tag.name);
        jb.arrayEnd();
        jb.objectEnd();

        return jb.build();
    }

    static void post(DBContext db, String json)
            throws APIException
    {

        try {
            Quiz quiz = fromJson(json);
            Quiz.save(db, quiz);
        } catch (JsonException e) {
            throw new APIException(400, e);
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }

    static void put(DBContext db, String json)
            throws APIException
    {
        try {
            Quiz quiz = fromJson(json);
            Quiz.update(db, quiz);
        } catch (JsonException e) {
            throw new APIException(400, e);
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }

    private static String getQueryParam(String key, String value,
                                        Map<String, List<String>> query)
    {
        List<String> values;
        values = query.get(key);
        if (values == null || values.isEmpty())
            return value;
        return values.get(0);
    }

    static String get(DBContext db, Map<String, List<String>> query)
            throws APIException
    {
        JsonBuilder jb = new JsonBuilder();
        List<Quiz> quizzes;

        try {
            int page;
            String sortKey;
            String sortOrder;
            String tag;
            String category;

            page = Integer.parseInt(getQueryParam("page", "1", query));
            sortKey = getQueryParam("sort-key", "id", query);
            sortOrder = getQueryParam("sort-order", "desc", query);
            tag = getQueryParam("tag", null, query);
            category = getQueryParam("category", null, query);
            if (page < 1)
                throw new APIException(400, "page < 1");

            quizzes = Quiz.loadByQuery(db,
                    page-1,
                    sortKey, sortOrder,
                    tag, category);
        } catch (InvalidParameterException|NumberFormatException e) {
            throw new APIException(400, e);
        } catch (DBException e) {
            throw new APIException(500, e);
        }

        jb.arrayBegin();
        if (quizzes != null) {
            for (Quiz quiz : quizzes) {
                jb.value(toJson(quiz));
            }
        }
        jb.arrayEnd();

        return jb.build();
    }

    static String get(DBContext db, Integer id)
        throws APIException
    {
        try {
            Quiz quiz;

            quiz = Quiz.load(db, id);
            if (quiz == null)
                throw new APIException(404, "not found");

            return toJson(quiz);
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }
}
