
package cn.xunhou.web.xbbcloud.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.Maps;
import org.apache.commons.lang.time.DateUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final String SECRET = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDFiErVWa8++SQefR0CiLC4VCtj\n7gMY4ylQ5WR2gvI/nJ10hsyooNhaqxD1pn5ovXsnIjM3dSU0AJPFSV14abh34HXN\nZ4TktMMs8oAPOeq5nrXyG8g2zjtYOiu6e43WAQnfYNGQ+SFSkZiYB2V1e6YRuk5C\nAh7XxHb5VQbnvEaiFQIDAQAB";
    public static final int EXPIRE_TIME = 30 * 24 * 60;

    private static final String ISSUER = "star_jwt";

    public static void main(String[] args) {
        Map<String, String> claims = new HashMap();
        claims.put("user_id", "1");
        String auth = genJwt(claims);
        claims = verifyToken(auth);
        System.out.println(claims);
    }

    public static String genJwt(Map<String, String> claims) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTCreator.Builder builder = JWT.create().withIssuer(ISSUER).withExpiresAt(DateUtils.addMinutes(new Date(), EXPIRE_TIME));
            claims.forEach(builder::withClaim);
            return builder.sign(algorithm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> verifyToken(String token) {
        Map<String, String> ret = Maps.newHashMap();
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
            DecodedJWT jwt = verifier.verify(token);
            Map<String, Claim> map = jwt.getClaims();
            map.forEach((k, v) -> ret.put(k, v.asString()));
            ret.put("exp", map.get("exp").asDate().toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return ret;
    }
}