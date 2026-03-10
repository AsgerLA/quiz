package app.web;

import app.db.Account;
import app.db.DBContext;
import app.db.DBException;
import app.web.json.JsonBuilder;
import app.web.json.JsonException;
import app.web.json.JsonObject;
import app.web.json.JsonParser;

class ApiUser
{
    static void post(DBContext db, String json)
        throws APIException
    {
        JsonObject jo;
        Account account;

        try {
            jo = JsonParser.decodeObject(json);

            account = new Account(jo.getString("username"));

            Account.create(db, account);
        } catch (JsonException e) {
            throw new APIException(400, e);
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }

    static String get(DBContext db, Integer id)
        throws APIException
    {
        JsonBuilder jb;
        Account account;

        try {
            account = Account.read(db, id);
            if (account == null)
                throw new APIException(404, "account not found");
        } catch (DBException e) {
            throw new APIException(500, e);
        }

        jb = new JsonBuilder();

        jb.objectBegin();
        jb.field("id", account.id);
        jb.field("username", account.username);
        jb.objectEnd();

        return jb.build();
    }
}
