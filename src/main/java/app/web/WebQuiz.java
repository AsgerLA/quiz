package app.web;

import io.javalin.apibuilder.EndpointGroup;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;
import static io.javalin.apibuilder.ApiBuilder.delete;
import io.javalin.http.Context;

import app.db.*;
import app.web.json.*;

import java.util.List;

public class WebQuiz
{
    static EndpointGroup routes()
    {
        return () ->{
            get("/", WebQuiz::GET_index);
            get("/quiz/{id}", WebQuiz::GET_quiz);
            post("/quiz", WebQuiz::POST_createQuiz);
        };
    }
    private static String toJson(Quiz quiz)
    {
        JsonBuilder jb = new JsonBuilder();
        jb.objectBegin();
        jb.field("id", quiz.id);
        jb.field("title", quiz.title);
        jb.objectEnd();
        return jb.build();
    }

    private static String toJson(Question question)
    {
        JsonBuilder jb = new JsonBuilder();
        jb.objectBegin();
        jb.field("id", question.id);
        jb.field("question", question.question);
        jb.field("slot", question.slot);
        jb.field("categoryId", question.category.id);
        jb.objectEnd();
        return jb.build();
    }
    private static String toJson(Answer answer)
    {
        JsonBuilder jb = new JsonBuilder();
        jb.objectBegin();
        jb.field("id", answer.id);
        jb.field("answer", answer.answer);
        jb.field("slot", answer.slot);
        jb.field("points", answer.points);
        jb.objectEnd();
        return jb.build();
    }

    public static void GET_index(Context ctx)
            throws DBException
    {
        JsonBuilder jb = new JsonBuilder();
        List<Quiz> quizzes = Quiz.loadAll(Web.db);
        String json;

        jb.arrayBegin();
        for (Quiz quiz : quizzes)
            jb.append(toJson(quiz));
        jb.arrayEnd();
        json = jb.build();
        System.out.println(json);

        ctx.json(json);
    }

    public static void POST_createQuiz(Context ctx)
            throws JsonException, DBException
    {
        JsonObject jo;
        JsonArray ja;
        String json;

        json = ctx.body();
        jo = JsonParser.decodeObject(json);

        // add new quiz
        Quiz quiz = new Quiz(jo.getString("title"));

        ja = jo.getJsonArray("questions");
        for (int i = 0; i < ja.size(); i++) {
            JsonObject tmp = ja.getJsonObject(i);
            Category cat = Category.load(Web.db, tmp.getInt("categoryId"));
            Question question = new Question(
                    quiz,
                    tmp.getString("question"),
                    cat,
                    tmp.getInt("slot"));
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

        Quiz.save(Web.db, quiz);

        ctx.status(200);
    }

    public static void GET_quiz(Context ctx)
            throws DBException, JsonException
    {
        Integer id;
        JsonBuilder jb = new JsonBuilder();
        Quiz quiz;
        List<Question> questions;

        id = Integer.parseInt(ctx.pathParam("id"));

        quiz = Quiz.load(Web.db, id);
        questions = Question.loadByQuizId(Web.db, quiz.id);

        jb.objectBegin("quiz");
        jb.append(toJson(quiz));
        jb.objectBegin("questions");
        for (Question question : questions) {
            jb.append(toJson(question));
            jb.arrayBegin("answers");
            for (Answer answer : question.answers)
                jb.append(toJson(answer));
            jb.arrayEnd();
        }
        jb.objectEnd();

        String json = jb.build();

        ctx.json(json);
    }
}
