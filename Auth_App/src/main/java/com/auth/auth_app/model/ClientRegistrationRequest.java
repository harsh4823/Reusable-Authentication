package com.auth.auth_app.model;

import java.util.List;

public record ClientRegistrationRequest(
    String clientName,
    List<String> redirectUris,
    List<String> scopes,
    List<String> grantTypes
) {}