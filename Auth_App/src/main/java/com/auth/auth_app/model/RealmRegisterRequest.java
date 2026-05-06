package com.auth.auth_app.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RealmRegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String name
) {}