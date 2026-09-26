package com.auth.auth_app.strategy.impl;

import com.auth.auth_app.Exception.ResourceNotFoundException;
import com.auth.auth_app.entity.AuthMethod;
import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.Realm;
import com.auth.auth_app.entity.Role;
import com.auth.auth_app.model.RealmLoginRequest;
import com.auth.auth_app.model.RealmRegisterRequest;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.strategy.IRealmAuthStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PasswordAuthStrategy implements IRealmAuthStrategy {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(AuthMethod authMethod) {
        return authMethod == AuthMethod.EMAIL_PASSWORD;
    }

    @Override
    public AuthUser authenticate(RealmLoginRequest request) {
        AuthUser authUser = authUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("AuthUser", "email", request.email()));

        if (!passwordEncoder.matches(request.password(), authUser.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return authUser;
    }

    @Override
    public AuthUser registerNewUser(RealmRegisterRequest request, Realm realm, Role defaultRole) {
        if (request.password() == null || request.password().isBlank()) {
            throw new BadCredentialsException("Password is required for this realm");
        }

        return AuthUser.builder()
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .memberRealm(realm)
                .roles(new HashSet<>(Set.of(defaultRole)))
                .build();
    }
}