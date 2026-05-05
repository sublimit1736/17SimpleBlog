package cn.chunana.simblog17api.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class TokenSecurityService {

    private static final String REVOKED_PREFIX = "auth:revoked:";
    private static final String RATE_LIMIT_PREFIX = "auth:rl:";

    private final StringRedisTemplate stringRedisTemplate;

    public void revokeToken(String token, long ttlMillis) {
        if (isBlank(token) || ttlMillis <= 0) {
            return;
        }

        stringRedisTemplate.opsForValue().set(revokedKey(token), "1", Duration.ofMillis(ttlMillis));
    }

    public boolean isTokenRevoked(String token) {
        if (isBlank(token)) {
            return false;
        }
        Boolean found = stringRedisTemplate.hasKey(revokedKey(token));
        return Boolean.TRUE.equals(found);
    }

    public boolean tryAcquireRateLimit(String bucketKey, long maxAttempts, long windowSeconds) {
        if (isBlank(bucketKey) || maxAttempts <= 0 || windowSeconds <= 0) {
            return false;
        }

        String key = RATE_LIMIT_PREFIX + bucketKey;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count == null) {
            return true;
        }

        if (count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        return count <= maxAttempts;
    }

    private String revokedKey(String token) {
        return REVOKED_PREFIX + sha256(token);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}


