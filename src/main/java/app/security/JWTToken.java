package app.security;

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

public class JWTToken
{
    private static final long TOKEN_EXPIRE_TIME = 1800000;
    public static String create(String subject, String secret)
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

    public static String verify(String token, String secret)
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
