package cn.chunana.simblog17api.utils;

import cn.chunana.simblog17api.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    private static final String ACCESS_TOKEN_TYPE  = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Value("${app.jwt.issuer:simblog17api}")
    private String issuer;

    @Value("${app.jwt.clock-skew-seconds:30}")
    private long clockSkewSeconds;

    @PostConstruct
    void validateSecret() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("app.jwt.issuer must not be blank");
        }
        if (accessExpirationMs < 60_000L) {
            throw new IllegalStateException("app.jwt.expiration-ms must be at least 60000");
        }
        if (refreshExpirationMs < accessExpirationMs) {
            throw new IllegalStateException("app.jwt.refresh-expiration-ms must be greater than or equal to app.jwt.expiration-ms");
        }
        if (clockSkewSeconds < 0 || clockSkewSeconds > 300) {
            throw new IllegalStateException("app.jwt.clock-skew-seconds must be between 0 and 300");
        }
    }

    public String generateAccessToken(User user) {
        return generateToken(user, ACCESS_TOKEN_TYPE, accessExpirationMs);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, REFRESH_TOKEN_TYPE, refreshExpirationMs);
    }

    private String generateToken(User user, String tokenType, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                   .id(UUID.randomUUID().toString())
                   .subject(user.getId().toString())
                   .issuer(issuer)
                   .claim("tokenType", tokenType)
                   .claim("username", user.getUsername())
                   .claim("role", user.getRole().name())
                   .issuedAt(now)
                   .expiration(new Date(now.getTime() + expirationMs))
                   .signWith(signingKey())
                   .compact();
    }

    public Claims parseAccessToken(String token) {
        return parseToken(token, ACCESS_TOKEN_TYPE);
    }

    public Claims parseRefreshToken(String token) {
        return parseToken(token, REFRESH_TOKEN_TYPE);
    }

    private Claims parseToken(String token, String requiredType) {
        Claims claims = Jwts.parser()
                            .verifyWith(signingKey())
                            .requireIssuer(issuer)
                            .clockSkewSeconds(clockSkewSeconds)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

        String subject   = claims.getSubject();
        String tokenType = claims.get("tokenType", String.class);
        String role      = claims.get("role", String.class);
        if (subject == null || subject.isBlank()
            || role == null || role.isBlank()
            || tokenType == null || !requiredType.equals(tokenType)
            || claims.getId() == null || claims.getId().isBlank()) {
            throw new IllegalArgumentException("Invalid JWT claims");
        }
        return claims;
    }

    public long remainingAccessValidityMillis(String token) {
        long remaining = parseAccessToken(token).getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }

    public long remainingRefreshValidityMillis(String token) {
        long remaining = parseRefreshToken(token).getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

