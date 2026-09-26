package com.auth.auth_app.strategy.impl;

import com.auth.auth_app.Exception.ResourceNotFoundException;
import com.auth.auth_app.entity.AuthMethod;
import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.Realm;
import com.auth.auth_app.entity.Role;
import com.auth.auth_app.model.RealmLoginRequest;
import com.auth.auth_app.model.RealmRegisterRequest;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.service.IOtpAuthenticationService;
import com.auth.auth_app.strategy.IRealmAuthStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OtpAuthStrategy implements IRealmAuthStrategy {

    private final AuthUserRepository authUserRepository;
    private final IOtpAuthenticationService otpService;

    @Override
    public boolean supports(AuthMethod authMethod) {
        return authMethod == AuthMethod.EMAIL_OTP;
    }

    @Override
    public AuthUser authenticate(RealmLoginRequest request) {
        if (!otpService.validateOtp(request.email(), request.otp())) {
            throw new BadCredentialsException("Invalid or expired OTP");
        }

        return authUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("AuthUser", "email", request.email()));
    }

    @Override
    public AuthUser registerNewUser(RealmRegisterRequest request, Realm realm, Role defaultRole) {
        if (request.otp() == null || !otpService.validateOtp(request.email(), request.otp())) {
            throw new BadCredentialsException("Invalid or expired OTP for registration");
        }

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