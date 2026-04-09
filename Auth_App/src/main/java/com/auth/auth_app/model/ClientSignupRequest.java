package com.auth.auth_app.model;

public record ClientSignupRequest(
        String email,
        String password,
        String name,
        String appName,
        String realmName,
        String redirectUri
) {
}
