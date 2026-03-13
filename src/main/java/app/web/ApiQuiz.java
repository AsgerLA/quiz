package app.web;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.db.Category;
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

    static String get(DBContext db,
                      Map<String, List<String>> query,
                      Integer ownerId)
            throws APIException
    {
        JsonBuilder jb = new JsonBuilder();
        List<Quiz> quizzes;

        try {
            quizzes = Quiz.loadByQuery(db, new Quiz.QueryParam(query), ownerId);
        } catch (InvalidParameterException e) {
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

    static String getSearch(DBContext db, String search, Integer page)
            throws APIException
    {
        JsonBuilder jb = new JsonBuilder();
        List<Quiz> quizzes;

        if (search == null)
            throw new APIException(400, "missing search query");
        if (page == null)
            page = 1;
        try {
            quizzes = Quiz.loadBySearch(db, search, page);
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
}
