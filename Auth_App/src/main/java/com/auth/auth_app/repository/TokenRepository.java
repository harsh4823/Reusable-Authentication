package com.auth.auth_app.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TokenRepository {
    private final RedisTemplate redisTemplate;

    private static final String ACCESS_TOKEN_KEY_PREFIX = "user:access:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "user:refresh:";
}
