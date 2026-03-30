package com.auth.auth_app.Exception;

public class Oauth2MissingEmailException extends RuntimeException {
    // RuntimeException is better than Throwable — works with Spring's exception handling
    private final String providerName;
    private final String providerId;

    public Oauth2MissingEmailException(String providerName, String providerId) {
        super("OAuth2 provider did not return an email address.");
        this.providerName = providerName;
        this.providerId = providerId;
    }

    public String getProviderName() { return providerName; }
    public String getProviderId()   { return providerId; }
}
