package com.auth.auth_app.model;

import jakarta.validation.constraints.Email;

public record RealmLoginRequest(
        @Email String email,
        String password,
        String otp,
        String qrSessionId
) {}
