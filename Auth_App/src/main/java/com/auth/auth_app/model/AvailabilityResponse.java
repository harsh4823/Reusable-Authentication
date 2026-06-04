package com.auth.auth_app.model;

public record AvailabilityResponse(
        boolean available,
        String message
) {}