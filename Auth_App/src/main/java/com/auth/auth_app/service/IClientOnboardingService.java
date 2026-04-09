package com.auth.auth_app.service;

import com.auth.auth_app.model.ClientSignupRequest;
import com.auth.auth_app.model.ClientSignupResponse;

public interface IClientOnboardingService {
    ClientSignupResponse registerClientDeveloper(ClientSignupRequest request);
}
