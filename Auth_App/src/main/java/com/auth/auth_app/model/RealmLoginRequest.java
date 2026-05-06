package com.auth.auth_app.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RealmLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {}
