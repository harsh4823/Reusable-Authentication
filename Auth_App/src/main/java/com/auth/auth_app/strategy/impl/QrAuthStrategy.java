package com.auth.auth_app.strategy.impl;

import com.auth.auth_app.Exception.ResourceNotFoundException;
import com.auth.auth_app.entity.AuthMethod;
import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.Realm;
import com.auth.auth_app.entity.Role;
import com.auth.auth_app.model.RealmLoginRequest;
import com.auth.auth_app.model.RealmRegisterRequest;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.service.IQRAuthenticationService;
import com.auth.auth_app.strategy.IRealmAuthStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class QrAuthStrategy implements IRealmAuthStrategy {

    private final AuthUserRepository authUserRepository;
    private final IQRAuthenticationService qrService;

    @Override
    public boolean supports(AuthMethod authMethod) {
        return authMethod == AuthMethod.QR_CODE;
    }

    @Override
    public AuthUser authenticate(RealmLoginRequest request) {
        String authenticatedEmail = qrService.consumeSession(request.qrSessionId());

        return authUserRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("AuthUser", "email", authenticatedEmail));
    }

    @Override
    public AuthUser registerNewUser(RealmRegisterRequest request, Realm realm, Role defaultRole) {
        return AuthUser.builder()
                .email(request.email())
                .name(request.name())
                .password(null)
                .enabled(true)
                .memberRealm(realm)
                .roles(new HashSet<>(Set.of(defaultRole)))
                .build();
    }
}