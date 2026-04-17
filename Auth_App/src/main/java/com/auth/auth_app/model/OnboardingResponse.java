package com.auth.auth_app.model;

public record OnboardingResponse (
   String message,
   String ownerEmail,
   String realmName,
   String clientId,
   String clientSecret,
   String wellKnownUrl,
   String tokenEndPoint
) {}
