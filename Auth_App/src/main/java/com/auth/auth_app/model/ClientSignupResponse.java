package com.auth.auth_app.model;

public record ClientSignupResponse(
        String message,
        String realmName,
        String clientId,
        String clientSecret,
        String dashboardUri
) {
}
