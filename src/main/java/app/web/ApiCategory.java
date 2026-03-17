package app.web;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import app.db.Category;
import app.db.DBContext;
import app.db.DBException;
import app.db.Quiz;
import app.db.Tag;
import app.web.json.JsonBuilder;

class ApiCategory
{
    static void buildCache(DBContext db)
            throws APIException
    {
        try {
            List<Category> cats = Category.loadAll(db);
            ApiCategory.cacheStart(db);
            for (Category cat : cats)
                ApiCategory.cacheCategory(db, cat.tag.name);
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }

    static String get(DBContext db)
            throws APIException
    {
        String json;

        json = cacheJson;
        if (json == null) {
            cacheStart(db);
            json = cacheJson;
        }
        return json;
    }

    static String getCategory(DBContext db, String category)
            throws APIException
    {
        String json;

        json = cache.get(category);
        if (json == null) {
            cacheCategory(db, category);
            json = cache.get(category);
        }

        return json;
    }

    private static void addSection(JsonBuilder jb, String name, List<Quiz> quizzes)
    {
        jb.objectBegin();
        jb.field("name", name);
        jb.arrayBegin("quizzes");
        for (Quiz quiz : quizzes) {
            jb.json(ApiQuiz.toJson(quiz));
        }
        jb.arrayEnd();
        jb.objectEnd();
    }

    private volatile static String cacheJson = null;
    private synchronized static void cacheStart(DBContext db)
            throws APIException
    {
        try {
            JsonBuilder jb;
            List<Quiz> quizzes;
            Quiz.QueryParam query;
            List<Category> categories;

            categories = Category.loadAll(db);
            jb = new JsonBuilder();

            query = new Quiz.QueryParam();
            query.pageSize = 4;

            jb.objectBegin();
            jb.arrayBegin("categories");
            for (Category cat : categories) {
                jb.value(cat.tag.name);
            }
            jb.arrayEnd();

            jb.arrayBegin("sections");
            for (Category cat : categories) {
                query.category = cat.tag.name;
                quizzes = Quiz.loadByQuery(db, query, null);
                if (quizzes == null || quizzes.isEmpty())
                    continue;

                addSection(jb, cat.tag.name, quizzes);
            }
            jb.arrayEnd();
            jb.objectEnd();

            cacheJson = jb.build();
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }


    private static Map<String, String> cache = new ConcurrentHashMap<>();
    private static void cacheCategory(DBContext db, String category)
            throws APIException
    {
        try {
            JsonBuilder jb;
            List<Quiz> quizzes;
            Quiz.QueryParam query;
            List<Category> categories;
            List<Tag> tags = null;

            categories = Category.loadAll(db);
            jb = new JsonBuilder();

            query = new Quiz.QueryParam();
            query.pageSize = 4;

            jb.objectBegin();
            jb.arrayBegin("categories");
            for (Category cat : categories) {
                jb.value(cat.tag.name);
                if (cat.tag.name.equals(category)) {
                    query.category = cat.tag.name;
                    tags = Category.loadSubTags(db, cat.id);
                }
            }
            jb.arrayEnd();
            if (query.category == null)
                throw new APIException(404, "bad category");

            jb.arrayBegin("tags");
            for (Tag tag : tags)
                jb.value(tag.name);
            jb.arrayEnd();

            jb.arrayBegin("sections");

            query.sort = "playCount";
            quizzes = Quiz.loadByQuery(db, query, null);
            if (quizzes != null && !quizzes.isEmpty()) {
                addSection(jb, query.sort, quizzes);
            }

            query.sort = "created";
            quizzes = Quiz.loadByQuery(db, query, null);
            if (quizzes != null && !quizzes.isEmpty()) {
                addSection(jb, query.sort, quizzes);
            }

            query.sort = "rating";
            quizzes = Quiz.loadByQuery(db, query, null);
            if (quizzes != null && !quizzes.isEmpty()) {
                addSection(jb, query.sort, quizzes);
            }

            jb.arrayEnd();

            jb.objectEnd();

            cache.put(category, jb.build());
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }
}
