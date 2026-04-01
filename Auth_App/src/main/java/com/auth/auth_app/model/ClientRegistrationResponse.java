package com.auth.auth_app.model;

import java.util.List;

public record ClientRegistrationResponse(
    String clientId,
    String clientSecret,
    String clientName,
    List<String> redirectUris,
    List<String> scopes,
    List<String> grantTypes,
    String createdAt
) {}