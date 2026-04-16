package com.auth.auth_app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserInfoResponse(
        String sub,
        String email,
        String name,
        String picture,
        String realm,
        @JsonProperty("email_verified") boolean emailVerified
) {
}
