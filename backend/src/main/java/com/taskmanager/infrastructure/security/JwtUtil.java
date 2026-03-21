package com.taskmanager.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.security.Key;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            Object parserOrBuilder = invokeStaticNoArgs(Jwts.class, "parserBuilder", "parser");

            // Configure key using whichever method is available in the resolved JJWT version.
            if (hasMethod(parserOrBuilder, "setSigningKey", Key.class)) {
                parserOrBuilder = invoke(parserOrBuilder, "setSigningKey", new Class<?>[]{Key.class}, getSigningKey());
            } else if (hasMethod(parserOrBuilder, "verifyWith", SecretKey.class)) {
                parserOrBuilder = invoke(parserOrBuilder, "verifyWith", new Class<?>[]{SecretKey.class}, getSigningKey());
            }

            Object parser = hasMethod(parserOrBuilder, "build")
                    ? invokeNoArgs(parserOrBuilder, "build")
                    : parserOrBuilder;

            if (hasMethod(parser, "parseClaimsJws", String.class)) {
                Object jws = invoke(parser, "parseClaimsJws", new Class<?>[]{String.class}, token);
                return (Claims) invokeNoArgs(jws, "getBody");
            }

            Object jws = invoke(parser, "parseSignedClaims", new Class<?>[]{String.class}, token);
            return (Claims) invokeNoArgs(jws, "getPayload");
        } catch (Exception ex) {
            log.error("Failed to parse JWT claims", ex);
            throw new JwtException("Invalid JWT token", ex);
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static Object invokeStaticNoArgs(Class<?> type, String... methodCandidates) throws Exception {
        for (String methodName : methodCandidates) {
            try {
                Method method = type.getMethod(methodName);
                return method.invoke(null);
            } catch (NoSuchMethodException ignored) {
                // Try next candidate.
            }
        }
        throw new NoSuchMethodException("No parser factory method found on " + type.getName());
    }

    private static Object invokeNoArgs(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object argument) throws Exception {
        Method method = target.getClass().getMethod(methodName, parameterTypes);
        return method.invoke(target, argument);
    }

    private static boolean hasMethod(Object target, String methodName, Class<?>... parameterTypes) {
        try {
            target.getClass().getMethod(methodName, parameterTypes);
            return true;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }
}
