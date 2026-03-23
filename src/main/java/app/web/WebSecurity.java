package app.web;

import static io.javalin.apibuilder.ApiBuilder.post;

import java.util.Date;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import app.db.Account;
import app.db.DBContext;
import app.web.json.JsonBuilder;
import app.web.json.JsonException;
import app.web.json.JsonObject;
import app.web.json.JsonParser;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

class WebSecurity
{
    WebSecurity(DBContext db)
    {
        this.db = db;
        SECRET_KEY = System.getenv("SECRET_KEY");
    }
    private DBContext db;
    private final String SECRET_KEY;

    EndpointGroup routes()
    {
        return () -> {
            post("/api/auth/signin", this::signin);
            post("/api/auth/signup", this::signup);
        };
    }

    boolean authorize(Context ctx, boolean requireAdmin)
    {
        String token;
        String subject;

        token = getToken(ctx);
        if (authorizeAdmin(token)) {
            return true;
        } else if (requireAdmin) {
            Result.notFound(ctx);
            return false;
        }

        if (token == null) {
            Result.error(ctx, 401, "bad Authorization header");
            return false;
        }

        subject = verifyJWT(token, SECRET_KEY);
        if (subject == null) {
            Result.error(ctx, 401, "token invalid");
            return false;
        }

        Account account = new Account();
        account.id = parseInt(subject);
        if (account.id == null) {
            Result.unauthorized(ctx);
            return false;
        }

        ctx.attribute("account", account);
        return true;
    }

    boolean authorizeAdmin(String token)
    {
        if (token == null)
            return false;

        return token.equals(SECRET_KEY);
    }

    private String getToken(Context ctx)
    {
        int i;
        String header;
        String token;
        String type;

        header = ctx.header("Authorization");
        if (header == null)
            return null;

        i = 0;
        while (i < header.length() && header.charAt(i) != ' ')
            i++;
        if (i+1 >= header.length()) {
            return null;
        }
        type = header.substring(0, i);
        if (!type.equals("Bearer"))
            return null;

        token = header.substring(i+1, header.length());

        return token;
    }

    private static Integer parseInt(String s)
    {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    void signin(Context ctx)
    {
        String json;
        JsonObject jo;
        String username;
        String password;
        Account account;
        String token;
        JsonBuilder jb;

        json = ctx.body();
        if (json == null) {
            Result.error(ctx, 400, "missing JSON body");
            return;
        }
        try {
            jo = JsonParser.decodeObject(json);
            username = jo.getString("username");
            password = jo.getString("password");
        } catch (JsonException e) {
            Result.error(ctx, 400, e.getMessage());
            return;
        }

        account = Account.signin(db, username, password);
        if (account == null) {
            Result.error(ctx, 401, "wrong username or password");
            return;
        }

        token = createJWT(account.id.toString(), SECRET_KEY);
        jb = new JsonBuilder();
        jb.objectBegin();
            jb.field("token", token);
        jb.objectEnd();

        json = jb.build();

        Result.ok(ctx, json);
    }

    void signup(Context ctx)
    {
        String json;
        JsonObject jo;
        String username;
        String password;
        Integer id;

        json = ctx.body();
        if (json == null) {
            Result.error(ctx, 400, "missing JSON body");
            return;
        }
        try {
            jo = JsonParser.decodeObject(json);
            username = jo.getString("username");
            password = jo.getString("password");
        } catch (JsonException e) {
            Result.error(ctx, 400, e.getMessage());
            return;
        }

        id = Account.signup(db, username, password);
        if (id == null) {
            Result.conflict(ctx);
            return;
        }

        Result.noContent(ctx);
    }

    private static final long TOKEN_EXPIRE_TIME = 1800000;
    private static String createJWT(String subject, String secret)
    {
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .expirationTime(new Date(new Date().getTime() + TOKEN_EXPIRE_TIME))
                .build();
            Payload payload = new Payload(claimsSet.toJSONObject());

            JWSSigner signer = new MACSigner(secret);
            JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
            JWSObject jwsObject = new JWSObject(jwsHeader, payload);
            jwsObject.sign(signer);
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private static String verifyJWT(String token,
                                    String secret)
    {
        try {
            JWTClaimsSet claims;
            SignedJWT jwt = SignedJWT.parse(token);
            claims = jwt.getJWTClaimsSet();

            if (!jwt.verify(new MACVerifier(secret)))
                return null;

            if (claims.getExpirationTime().getTime()
                    - new Date().getTime() < 0)
                return null;

            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
