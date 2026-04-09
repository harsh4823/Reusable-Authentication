package com.auth.auth_app.service.impl;

import com.auth.auth_app.model.ClientSignupRequest;
import com.auth.auth_app.model.ClientSignupResponse;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.service.IClientOnboardingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientOnboardingServiceImp implements IClientOnboardingService {

    private final AuthUserRepository authUserRepository;

    @Override
    @Transactional
    public ClientSignupResponse registerClientDeveloper(ClientSignupRequest request) {

        if (authUserRepository.findByEmail(request.email()).isPresent()){
            throw new RuntimeException("Email already registered : " + request.email());
        }
        return null;
    }
}
