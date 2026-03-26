package app.db;

import java.util.ArrayList;
import java.util.List;

public class Catalog
{
    public static record Section(String name, List<Quiz> quizzes) {}
    public List<Category> categories;
    public List<Section> sections = new ArrayList<>();
    public List<Tag> tags;

    public static Catalog load(DBContext db)
            throws DBException
    {
        Catalog catalog;
        List<Category> categories;
        List<Quiz> quizzes;

        catalog = new Catalog();

        for (String sort : sorts) {
            quizzes = Quiz.loadTopByAttribute(db, sort);
            catalog.sections.add(new Section(sort, quizzes));
        }

        categories = Category.loadAll(db);
        catalog.categories = categories;
        for (Category cat : categories) {
            quizzes = Quiz.loadTopByTag(db, cat.tag.name);
            catalog.sections.add(new Section(cat.tag.name, quizzes));
        }

        return catalog;
    }

    private static final String[] sorts = new String[] {
        "playCount",
        "created",
        "vote"
    };
    public static Catalog loadByCategory(DBContext db, String categoryName)
            throws DBException
    {
        Catalog catalog;
        List<Category> categories;
        List<Quiz> quizzes;
        Integer categoryId = null;

        catalog = new Catalog();

        categories = Category.loadAll(db);
        for (Category cat : categories) {
            if (cat.tag.name.equals(categoryName)) {
                categoryId = cat.id;
                break;
            }
        }
        if (categoryId == null)
            return null;

        catalog.categories = categories;
        catalog.tags = Category.loadSubTags(db, categoryId);
        for (String sort : sorts) {
            quizzes = Quiz.loadTopByAttributeWithTag(db, categoryName, sort);
            catalog.sections.add(new Section(sort, quizzes));
        }

        return catalog;
    }
}
