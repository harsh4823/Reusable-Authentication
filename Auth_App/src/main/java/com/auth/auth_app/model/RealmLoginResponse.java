package com.auth.auth_app.model;

public record RealmLoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String realm
){}