package com.auth.auth_app.model;

import jakarta.validation.constraints.Email;

public record RealmRegisterRequest(
        String name,
        @Email String email,
        String password,
        String otp
) {}