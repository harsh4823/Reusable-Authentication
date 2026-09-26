package com.auth.auth_app.model;

public record RealmUserRequest(
        String email,
        String name,
        String password
) {}