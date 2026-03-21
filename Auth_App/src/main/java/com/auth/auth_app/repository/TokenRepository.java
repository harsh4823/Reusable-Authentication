package com.auth.auth_app.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class TokenRepository {
    private final RedisTemplate redisTemplate;

    private static final String ACCESS_TOKEN_KEY_PREFIX = "user:access:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "user:refresh:";

    private static final String ACCESS_BLACKLIST_PREFIX = "blacklist:access:";
    private static final String REFRESH_BLACKLIST_PREFIX = "blacklist:refresh:";

    private long jwtExpiration = 90000;

    private long refreshExpiration = 604800000L;

    public void storeTokens(Long userId , String jwtToken , String refreshToken){
        String accessTokenKey = ACCESS_TOKEN_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(accessTokenKey,jwtToken);
        redisTemplate.expire(accessTokenKey,jwtExpiration, TimeUnit.SECONDS);
        storeToken(accessTokenKey,jwtToken,jwtExpiration);

        String refreshTokenKey = REFRESH_TOKEN_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(refreshTokenKey,refreshToken);
        redisTemplate.expire(refreshTokenKey,refreshExpiration, TimeUnit.SECONDS);
        storeToken(refreshTokenKey,refreshToken,refreshExpiration);
    }

    public void storeToken(String key,String token,long expiration){
        redisTemplate.opsForValue().set(key,token);
        redisTemplate.expire(key,expiration, TimeUnit.SECONDS);
    }

    private String getAccessToken(Long userId){
        String accessKey = ACCESS_TOKEN_KEY_PREFIX + userId;
        return getToken(accessKey);
    }

    private String getRefreshToken(Long userId){
        String refreshKey = REFRESH_TOKEN_KEY_PREFIX + userId;
        return getToken(refreshKey);
    }

    private String getToken(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : value.toString();
    }

    public void removeAllTokens(Long userId){
        String accessToken = getAccessToken(userId);
        String refreshToken = getRefreshToken(userId);

        String accessTokenKey = ACCESS_TOKEN_KEY_PREFIX + userId;
        String refreshTokenKey = REFRESH_TOKEN_KEY_PREFIX + userId;

        redisTemplate.delete(accessTokenKey);
        redisTemplate.delete(refreshTokenKey);

        if (accessToken!=null){
            String accessBlackListKey = ACCESS_BLACKLIST_PREFIX + accessToken;
            blackListToken(accessBlackListKey,jwtExpiration);
        }

        if (refreshToken!=null){
            String refreshBlackListKey = REFRESH_BLACKLIST_PREFIX + accessToken;
            blackListToken(refreshBlackListKey,refreshExpiration);
        }
    }

    private void blackListToken(String key, long expiration) {
        redisTemplate.opsForValue().set(key,"blacklist");
        redisTemplate.expire(key,expiration, TimeUnit.SECONDS);
    }

    public boolean isAccessTokenBlacklisted(String token){
        String key = ACCESS_BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key);
    }

    public boolean isRefreshTokenBlacklisted(String token){
        String key = REFRESH_BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key);
    }
}
