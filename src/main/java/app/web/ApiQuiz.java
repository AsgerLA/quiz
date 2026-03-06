package app.web;

import app.db.*;
import app.web.json.*;

import java.util.List;

class ApiQuiz
{
    static String getlist(DBContext db, String tagname)
        throws APIException
    {
        try {
            JsonBuilder jb = new JsonBuilder();
            List<Quiz> quizzes;

            quizzes = Quiz.loadTop10ByTag(db, tagname);
            jb.arrayBegin();
            for (Quiz quiz : quizzes) {
                jb.objectBegin();
                jb.field("id", quiz.id);
                jb.field("title", quiz.title);
                jb.arrayBegin("tags");
                for (Tag tag : quiz.tags)
                    jb.value(tag.name);
                jb.arrayEnd();
                jb.objectEnd();
            }
            jb.arrayEnd();

            return jb.build();
        } catch (DBException e) {
            throw new APIException(500, e);
        }
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

    static String get(DBContext db, Integer id)
            throws APIException
    {
        JsonBuilder jb = new JsonBuilder();
        Quiz quiz;
        List<Question> questions;

        try {
            quiz = Quiz.load(db, id);
            if (quiz == null)
                throw new APIException(404, "not found");
            questions = Question.loadByQuizId(db, quiz.id);
            if (questions == null)
                throw new APIException(404, "not found");

            jb.objectBegin();
            jb.field("id", quiz.id);
            jb.field("title", quiz.title);
            jb.arrayBegin("questions");
            for (Question question : questions) {
                jb.objectBegin();
                jb.field("id", question.id);
                jb.field("question", question.question);
                jb.field("slot", question.slot);
                jb.arrayBegin("answers");
                for (Answer answer : question.answers) {
                    jb.objectBegin();
                    jb.field("id", answer.id);
                    jb.field("answer", answer.answer);
                    jb.field("slot", answer.slot);
                    jb.field("points", answer.points);
                    jb.objectEnd();
                }
                jb.arrayEnd();
                jb.objectEnd();
            }
            jb.arrayEnd();
            jb.objectEnd();
            return jb.build();
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }
}
