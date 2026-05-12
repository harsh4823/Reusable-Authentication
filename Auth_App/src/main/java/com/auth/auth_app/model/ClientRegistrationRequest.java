package com.auth.auth_app.model;

import java.util.List;

public record ClientRegistrationRequest(
    String clientName,
    String realmName,
    List<String> redirectUris,
    List<String> scopes,
    List<String> grantTypes
) {}