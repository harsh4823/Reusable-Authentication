package com.auth.auth_app.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OnboardingRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "App name is required")
        String appName,

        @NotBlank(message = "Realm name is required")
        String realmName,

        @NotBlank(message = "Redirect Uri is required")
        String redirectUri

) {}
