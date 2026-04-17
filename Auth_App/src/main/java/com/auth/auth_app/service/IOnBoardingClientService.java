package com.auth.auth_app.service;

import com.auth.auth_app.model.OnboardingRequest;
import com.auth.auth_app.model.OnboardingResponse;

public interface IOnBoardingClientService {
    OnboardingResponse onboard(OnboardingRequest request);
}
