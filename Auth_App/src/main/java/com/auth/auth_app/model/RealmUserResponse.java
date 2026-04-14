package com.auth.auth_app.model;

import java.time.LocalDate;
import java.util.Set;

public record RealmUserResponse(
        Long userId,
        String email,
        String name,
        String image,
        boolean enabled,
        Set<String> roles,
        LocalDate createdAt
)
{}
