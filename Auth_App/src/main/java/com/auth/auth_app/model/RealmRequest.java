package com.auth.auth_app.model;

public record RealmRequest(
        String realmName,
        String displayName,
        boolean enabled
) {}
