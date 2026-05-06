package com.auth.auth_app.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class TokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACCESS_TOKEN_KEY_PREFIX = "user:access:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "user:refresh:";

    private static final String ACCESS_BLACKLIST_PREFIX = "blacklist:access:";
    private static final String REFRESH_BLACKLIST_PREFIX = "blacklist:refresh:";

    private final long jwtExpirationInSeconds = 900;        // 15 minutes
    private final long refreshExpirationInSeconds = 604800; // 7 days

    public void storeTokens(Long userId, String jwtToken, String refreshToken) {

        String accessTokenKey = ACCESS_TOKEN_KEY_PREFIX + userId + ":" + jwtToken;
        String refreshTokenKey = REFRESH_TOKEN_KEY_PREFIX + userId + ":" + refreshToken;

        redisTemplate.opsForValue().set(accessTokenKey, "active", jwtExpirationInSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(refreshTokenKey, "active", refreshExpirationInSeconds, TimeUnit.SECONDS);
    }

    public void removeSingleSession(Long userId, String jwtToken, String refreshToken) {
        String accessTokenKey = ACCESS_TOKEN_KEY_PREFIX + userId + ":" + jwtToken;
        String refreshTokenKey = REFRESH_TOKEN_KEY_PREFIX + userId + ":" + refreshToken;

        redisTemplate.delete(accessTokenKey);
        redisTemplate.delete(refreshTokenKey);

        if (jwtToken != null) {
            redisTemplate.opsForValue().set(ACCESS_BLACKLIST_PREFIX + jwtToken, "blacklisted", jwtExpirationInSeconds, TimeUnit.SECONDS);
        }
        if (refreshToken != null) {
            redisTemplate.opsForValue().set(REFRESH_BLACKLIST_PREFIX + refreshToken, "blacklisted", refreshExpirationInSeconds, TimeUnit.SECONDS);
        }
    }

    public void removeAllTokens(Long userId) {
        Set<String> accessKeys = redisTemplate.keys(ACCESS_TOKEN_KEY_PREFIX + userId + ":*");
        Set<String> refreshKeys = redisTemplate.keys(REFRESH_TOKEN_KEY_PREFIX + userId + ":*");

        if (accessKeys != null && !accessKeys.isEmpty()) {
            for (String key : accessKeys) {
                String prefix = ACCESS_TOKEN_KEY_PREFIX + userId + ":";
                String token = key.substring(prefix.length());

                redisTemplate.opsForValue().set(ACCESS_BLACKLIST_PREFIX + token, "blacklisted", jwtExpirationInSeconds, TimeUnit.SECONDS);
                redisTemplate.delete(key);
            }
        }

        if (refreshKeys != null && !refreshKeys.isEmpty()) {
            for (String key : refreshKeys) {
                String token = key.substring(key.lastIndexOf(":") + 1);

                redisTemplate.opsForValue().set(REFRESH_BLACKLIST_PREFIX + token, "blacklisted", refreshExpirationInSeconds, TimeUnit.SECONDS);
                redisTemplate.delete(key);
            }
        }
    }

    public boolean isAccessTokenBlacklisted(String token) {
        String key = ACCESS_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean isRefreshTokenBlacklisted(String token) {
        String key = REFRESH_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}