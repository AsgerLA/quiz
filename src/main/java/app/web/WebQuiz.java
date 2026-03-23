package app.web;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;
import static io.javalin.apibuilder.ApiBuilder.delete;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

import app.db.Account;
import app.db.Answer;
import app.db.DBContext;
import app.db.Question;
import app.db.Quiz;
import app.db.Tag;
import app.web.json.JsonArray;
import app.web.json.JsonBuilder;
import app.web.json.JsonException;
import app.web.json.JsonObject;
import app.web.json.JsonParser;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

class WebQuiz
{
    WebQuiz(DBContext db, WebSecurity security)
    {
        this.db = db;
        this.security = security;
    }
    private DBContext db;
    private WebSecurity security;

    EndpointGroup routes()
    {
        return () -> {
            get("/api/quiz", this::GET_quiz);
            get("/api/quiz/search", this::GET_quiz_search);
            get("/api/quiz/user/{id}", this::GET_quiz_user);
            get("/api/quiz/{id}", this::GET_quiz_id);
            post("/api/quiz", this::POST_quiz);
            delete("/api/quiz/{id}", this::DELETE_quiz);
            put("/api/quiz", this::PUT_quiz);
        };
    }

    void GET_quiz(Context ctx)
    {
        /* ?category=<tag-name>
         * &tag=<tag-name>
         * &sort=<column>
         * &order=<desc|asc>
         * &page=<page-num>
         */
        List<Quiz> quizzes;
        Map<String, List<String>> query;
        String json;

        query = ctx.queryParamMap();
        try {
            quizzes = Quiz.loadByQuery(db,
                                       new Quiz.QueryParam(query),
                                       null);
        } catch (InvalidParameterException e) {
            Result.error(ctx, 400, "Invalid query parameter");
            return;
        }
        json = toJson(quizzes);

        Result.ok(ctx, json);
    }

    void GET_quiz_search(Context ctx)
    {
        List<Quiz> quizzes;
        String json;
        String search;
        Integer page;

        search = ctx.queryParam("query");
        page = parseInt(ctx.queryParam("page"));
        if (page == null) {
            Result.error(ctx, 400, "bad page number");
            return;
        }

        quizzes = Quiz.loadBySearch(db, search, page);
        json = toJson(quizzes);

        Result.ok(ctx, json);
    }

    void GET_quiz_user(Context ctx)
    {
        /* ?category=<tag-name>
         * &tag=<tag-name>
         * &sort=<column>
         * &order=<desc|asc>
         * &page=<page-num>
         */
        Map<String, List<String>> query;
        List<Quiz> quizzes;
        Integer ownerId;
        String json;

        ownerId = parseInt(ctx.pathParam("id"));
        if (ownerId == null) {
            Result.error(ctx, 400, "bad integer in path param");
            return;
        }

        query = ctx.queryParamMap();
        try {
            quizzes = Quiz.loadByQuery(db,
                                       new Quiz.QueryParam(query),
                                       ownerId);
        } catch (InvalidParameterException e) {
            Result.error(ctx, 400, "Invalied query parameter");
            return;
        }
        json = toJson(quizzes);

        Result.ok(ctx, json);
    }

    void GET_quiz_id(Context ctx)
    {
        Integer id;
        Quiz quiz;
        String json;

        id = parseInt(ctx.pathParam("id"));
        if (id == null) {
            Result.error(ctx, 400, "bad integer in path param");
            return;
        }

        quiz = Quiz.load(db, id);
        if (quiz == null) {
            Result.notFound(ctx);
            return;
        }
        json = toJson(quiz);

        Result.ok(ctx, json);
    }

    void POST_quiz(Context ctx)
    {
        String json;
        Account account;
        Quiz quiz;

        if (!security.authorize(ctx, false))
            return;

        account = ctx.attribute("account");
        if (account == null) {
            Result.error(ctx, 403, "requires an account");
            return;
        }
        json = ctx.body();
        if (json == null) {
            Result.error(ctx, 400, "missing JSON body");
            return;
        }

        try {
            quiz = fromJson(json);
            quiz.owner = account;
        } catch (JsonException e) {
            Result.error(ctx, 400, e.getMessage());
            return;
        }
        Quiz.create(db, quiz);

        Result.noContent(ctx);
    }

    void PUT_quiz(Context ctx)
    {
        if (!security.authorize(ctx, false))
            return;

        ctx.status(501);
    }

    void DELETE_quiz(Context ctx)
    {
        Account account;
        Integer id;

        if (!security.authorize(ctx, false))
            return;

        account = ctx.attribute("account");
        id = parseInt(ctx.pathParam("id"));
        if (id == null) {
            Result.badRequest(ctx);
            return;
        }

        if (!Quiz.delete(db, account, id)) {
            Result.notFound(ctx);
            return;
        }

        Result.noContent(ctx);
    }

    private static Integer parseInt(String s)
    {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static String toJson(Quiz quiz)
    {
        JsonBuilder jb = new JsonBuilder();

        jb.objectBegin();
        appendQuiz(jb, quiz);
        jb.objectEnd();

        return jb.build();
    }

    static String toJson(List<Quiz> quizzes)
    {
        JsonBuilder jb = new JsonBuilder();

        jb.arrayBegin();
        if (quizzes != null) {
            for (Quiz quiz : quizzes) {
                jb.objectBegin();
                 appendQuiz(jb, quiz);
                jb.objectEnd();
            }
        }
        jb.arrayEnd();

        return jb.build();
    }

    static String toJson(Quiz quiz, List<Question> questions)
    {
        JsonBuilder jb = new JsonBuilder();

        jb.objectBegin();
        appendQuiz(jb, quiz);
        jb.arrayBegin("questions");
        for (Question question : questions) {
            jb.objectBegin();
            appendQuestion(jb, question);
            jb.objectEnd();
        }
        jb.arrayEnd();

        return jb.build();
    }

    private static void appendQuiz(JsonBuilder jb, Quiz quiz)
    {
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
    }

    private static void appendQuestion(JsonBuilder jb, Question question)
    {
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
            Question.Type type;

            try {
                type = Question.Type.valueOf(tmp.getString("type"));
            } catch (IllegalArgumentException e) {
                throw new JsonException("Invalid value for type");
            }

            Question question = new Question(
                    quiz,
                    tmp.getString("question"),
                    tmp.getInt("slot"),
                    type);
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
}
