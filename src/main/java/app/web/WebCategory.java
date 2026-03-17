package app.web;

import static io.javalin.apibuilder.ApiBuilder.get;

import java.util.List;

import app.db.Category;
import app.db.DBContext;
import app.db.DBException;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

class WebCategory
{
    WebCategory(DBContext db)
        throws APIException
    {
        this.db = db;
        ApiCategory.buildCache(db);
    }
    private DBContext db;

    EndpointGroup routes()
    {
        return () -> {
            get("/api/category", this::GET_category);
            get("/api/category/{category}", this::GET_category_name);
        };
    }

    void GET_category(Context ctx)
        throws APIException
    {
        String json;

        json = ApiCategory.get(db);

        ctx.json(json);
    }

    void GET_category_name(Context ctx)
        throws APIException
    {
        String json;
        String category;

        category = ctx.pathParam("category");
        json = ApiCategory.getCategory(db, category);

        ctx.json(json);
    }
}
