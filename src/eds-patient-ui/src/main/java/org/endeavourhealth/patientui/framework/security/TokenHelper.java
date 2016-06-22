package org.endeavourhealth.patientui.framework.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.endeavourhealth.core.data.admin.models.EndUser;
import org.endeavourhealth.core.data.admin.models.Organisation;

import javax.naming.AuthenticationException;
import javax.ws.rs.core.NewCookie;
import java.time.Instant;
import java.util.*;

public class TokenHelper {
    private static final String TOKEN_TYPE = "typ";
    private static final String TOKEN_TYPE_JWT = "JWT";
    private static final String TOKEN_ISSUED_AT = "iat";
    private static final String TOKEN_NHS_NUMBER = "nhs";

    public static NewCookie createTokenAsCookie(String nhsNumber) {
        String token = createToken(nhsNumber);
        return createCookie(token);
    }

    private static String createToken(String nhsNumber) {
        Map<String, Object> bodyParameterMap = new HashMap<>();
        bodyParameterMap.put(TOKEN_ISSUED_AT, Long.toString(Instant.now().getEpochSecond()));
        bodyParameterMap.put(TOKEN_NHS_NUMBER, nhsNumber);

        JwtBuilder builder = Jwts.builder()
                .setHeaderParam(TOKEN_TYPE, TOKEN_TYPE_JWT)
                .setClaims(bodyParameterMap)
                .signWith(SignatureAlgorithm.HS256, SecurityConfig.TOKEN_SIGNING_SECRET);

        return builder.compact();
    }

    private static NewCookie createCookie(String token) {
        int maxAge = (int) (60L * SecurityConfig.TOKEN_EXPIRY_MINUTES); //a day
        long now = System.currentTimeMillis() + (1000 * maxAge);
        Date d = new Date(now);

        return new NewCookie(SecurityConfig.AUTH_COOKIE_NAME,
                token,
                SecurityConfig.AUTH_COOKIE_VALID_PATH,
                SecurityConfig.AUTH_COOKIE_VALID_DOMAIN,
                1,
                null,
                maxAge,
                d,
                SecurityConfig.AUTH_COOKIE_REQUIRES_HTTPS,
                false);
    }

    public static String validateToken(String token) throws Exception {
        Claims claims = Jwts
                .parser()
                .setSigningKey(SecurityConfig.TOKEN_SIGNING_SECRET)
                .parseClaimsJws(token)
                .getBody();

        long tokenIssuedMilliseconds = Long.parseLong((String) claims.get(TOKEN_ISSUED_AT)) * 1000L;
        Date tokenIssued = new Date(tokenIssuedMilliseconds);
        Date tokenExpiry = new Date(tokenIssuedMilliseconds + (SecurityConfig.TOKEN_EXPIRY_MINUTES * 60L * 1000L));

        if (Calendar.getInstance().getTime().after(tokenExpiry)) {
            throw new AuthenticationException("Token expired");
        }

        return (String)claims.get(TOKEN_NHS_NUMBER);
    }

}
