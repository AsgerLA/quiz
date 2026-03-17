package app.web;

import app.db.DBContext;

class ThreadCacheWriter
    extends Thread
{
    ThreadCacheWriter(DBContext db)
    {
        this.db = db;
    }

    private DBContext db;

    @Override
    public void run()
    {
        try {
            ApiCategory.buildCache(db);
        } catch (APIException e) {
            // too bad
        }
    }
}
