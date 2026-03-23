package app.web;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

import app.db.Catalog;
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
        Catalog catalog;

        catalog = Catalog.load(db);

        json = toJson(catalog);

        ctx.json(json);
    }

    void GET_category_name(Context ctx)
    {
        String json;
        String categoryName;
        Catalog catalog;

        categoryName = ctx.pathParam("category");
        catalog = Catalog.loadByCategory(db, categoryName);
        if (catalog == null) {
            Result.notFound(ctx);
            return;
        }
        json = toJson(catalog);

        ctx.json(json);
    }

    private static String toJson(Catalog catalog)
    {
        JsonBuilder jb;

        jb = new JsonBuilder();

        jb.objectBegin();
            jb.arrayBegin("categories");
            for (Category cat : catalog.categories) {
                jb.value(cat.tag.name);
            }
            jb.arrayEnd();

            if (catalog.tags != null) {
                jb.arrayBegin("tags");
                for (Tag tag : catalog.tags)
                    jb.value(tag.name);
                jb.arrayEnd();
            }

            jb.arrayBegin("sections");
            for (Catalog.Section section : catalog.sections) {
                jb.objectBegin();
                jb.field("name", section.name());
                jb.arrayBegin("quizzes");
                for (Quiz quiz : section.quizzes())
                    jb.json(WebQuiz.toJson(quiz));
                jb.arrayEnd();
                jb.objectEnd();
            }
            jb.arrayEnd();

        jb.objectEnd();

        return jb.build();
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
