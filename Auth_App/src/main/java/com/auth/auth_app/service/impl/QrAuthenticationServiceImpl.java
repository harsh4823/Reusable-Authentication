package com.auth.auth_app.service.impl;

import com.auth.auth_app.service.IQRAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrAuthenticationServiceImpl implements IQRAuthenticationService {

    private final StringRedisTemplate redisTemplate;
    
    private static final String REDIS_KEY_PREFIX = "qr_session:";
    private static final int QR_EXPIRATION_MINUTES = 2;

    public String initQrSession() {
        String sessionId = UUID.randomUUID().toString();
        String redisKey = REDIS_KEY_PREFIX + sessionId;

        redisTemplate.opsForValue().set(redisKey, "PENDING", Duration.ofMinutes(QR_EXPIRATION_MINUTES));
        
        return sessionId;
    }

    public String checkSessionStatus(String sessionId) {
        String redisKey = REDIS_KEY_PREFIX + sessionId;
        String status = redisTemplate.opsForValue().get(redisKey);
        
        return status != null ? status : "INVALID";
    }

    public void authorizeSession(String sessionId, String authenticatedUserEmail) {
        String redisKey = REDIS_KEY_PREFIX + sessionId;
        String status = redisTemplate.opsForValue().get(redisKey);
        
        if ("PENDING".equals(status)) {
            redisTemplate.opsForValue().set(
                redisKey, 
                "AUTHORIZED:" + authenticatedUserEmail, 
                Duration.ofMinutes(1)
            );
        } else {
            throw new IllegalStateException("Invalid or expired QR session");
        }
    }

    @Override
    public String consumeSession(String sessionId) {
        String redisKey = REDIS_KEY_PREFIX + sessionId;
        String status = redisTemplate.opsForValue().get(redisKey);
        
        if (status != null && status.startsWith("AUTHORIZED:")) {
            redisTemplate.delete(redisKey); // Prevent replay attacks (Single-use)
            return status.split(":")[1]; // Return the email to issue the OAuth2 token
        }
        
        throw new IllegalStateException("Session not authorized");
    }
}