package app.web;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

import app.db.DBContext;
import app.db.DBException;
import app.db.Quiz;
import app.db.Tag;
import app.web.json.JsonBuilder;

class ApiQuiz
{

    static String toJson(Quiz quiz)
    {
        JsonBuilder jb = new JsonBuilder();

        jb.objectBegin();
        jb.field("id", quiz.id);
        jb.field("title", quiz.title);
        jb.field("playCount", quiz.playCount);
        jb.field("voteAverage", quiz.voteAverage);
        jb.field("created", quiz.created.toString());
        jb.field("owner", quiz.owner.username);
        jb.arrayBegin("tags");
        for (Tag tag : quiz.tags)
            jb.value(tag.name);
        jb.arrayEnd();
        jb.objectEnd();

        return jb.build();
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
                jb.json(toJson(quiz));
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
