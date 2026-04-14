package com.auth.auth_app.model;

import java.util.Set;

public record RealmUserUpdateRequest (
        String name,
        boolean enabled,
        Set<String> roles
)
{}
