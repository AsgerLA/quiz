package app.web;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

import java.util.List;

import app.db.Category;
import app.db.DBContext;
import app.db.Quiz;
import app.db.Tag;
import app.web.json.JsonBuilder;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

class WebCategory
{
    WebCategory(DBContext db, WebSecurity security)
    {
        this.db = db;
        this.security = security;
    }
    private DBContext db;
    private WebSecurity security;

    EndpointGroup routes()
    {
        return () -> {
            get("/api/category", this::GET_category);
            get("/api/category/{category}", this::GET_category_name);
            post("/api/category/{category}", this::POST_category);
            delete("/api/category/{category}", this::DELETE_category);
        };
    }

    void GET_category(Context ctx)
    {
        String json;

        JsonBuilder jb;
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
            addCategory(jb, cat.tag.name);
        }
        jb.arrayEnd();

        jb.objectEnd();

        json = jb.build();

        ctx.json(json);
    }

    void GET_category_name(Context ctx)
    {
        String json;
        String categoryName;

        categoryName = ctx.pathParam("category");

        JsonBuilder jb;
        Quiz.QueryParam query;
        List<Category> categories;
        Category category = null;

        categories = Category.loadAll(db);
        jb = new JsonBuilder();

        query = new Quiz.QueryParam();
        query.pageSize = 4;

        jb.objectBegin();
        jb.arrayBegin("categories");
        for (Category cat : categories) {
            jb.value(cat.tag.name);
            if (cat.tag.name.equals(categoryName)) {
                category = cat;
            }
        }
        jb.arrayEnd();
        if (category == null) {
            ctx.status(404);
            return;
        }
        query.category = categoryName;

        addTags(jb, category.id);

        jb.arrayBegin("sections");
            addSection(jb, "playCount");
            addSection(jb, "created");
            addSection(jb, "rating");
        jb.arrayEnd();

        jb.objectEnd();

        json = jb.build();

        ctx.json(json);
    }

    private void addTags(JsonBuilder jb, Integer categoryId)
    {
        List<Tag> tags;
        tags = Category.loadSubTags(db, categoryId);

        jb.arrayBegin("tags");
        for (Tag tag : tags)
            jb.value(tag.name);
        jb.arrayEnd();
    }

    private void addSection(JsonBuilder jb, String sort)
    {
        List<Quiz> quizzes;
        Quiz.QueryParam query = new Quiz.QueryParam();
        query.sort = sort;
        quizzes = Quiz.loadByQuery(db, query, null);
        if (quizzes == null || quizzes.isEmpty())
            return;
        jb.objectBegin();
        jb.field("name", sort);
        jb.arrayBegin("quizzes");
        for (Quiz quiz : quizzes) {
            jb.json(WebQuiz.toJson(quiz));
        }
        jb.arrayEnd();
        jb.objectEnd();
    }

    private void addCategory(JsonBuilder jb, String categoryName)
    {
        List<Quiz> quizzes;
        Quiz.QueryParam query = new Quiz.QueryParam();
        query.category = categoryName;
        quizzes = Quiz.loadByQuery(db, query, null);
        if (quizzes == null || quizzes.isEmpty())
            return;
        jb.objectBegin();
        jb.field("name", categoryName);
        jb.arrayBegin("quizzes");
        for (Quiz quiz : quizzes) {
            jb.json(WebQuiz.toJson(quiz));
        }
        jb.arrayEnd();
        jb.objectEnd();
    }

    void POST_category(Context ctx)
    {
        String name;
        Category cat;
        Tag tag;

        if (!security.authorize(ctx, true))
            return;

        name = ctx.pathParam("category");

        tag = new Tag(name);
        Tag.create(db, tag);
        cat = new Category(tag);
        Category.create(db, cat);

        Result.noContent(ctx);
    }

    void DELETE_category(Context ctx)
    {
        String name;
        Category cat;

        if (!security.authorize(ctx, true))
            return;

        name = ctx.pathParam("category");

        cat = Category.loadByName(db, name);
        if (cat == null) {
            Result.notFound(ctx);
            return;
        }
        Category.delete(db, cat);

        Result.noContent(ctx);
    }
}
