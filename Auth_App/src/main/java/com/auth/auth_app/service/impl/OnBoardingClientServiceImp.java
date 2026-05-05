package com.auth.auth_app.service.impl;

import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.Realm;
import com.auth.auth_app.entity.Role;
import com.auth.auth_app.model.OnboardingRequest;
import com.auth.auth_app.model.OnboardingResponse;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.repository.RealmRepository;
import com.auth.auth_app.repository.RoleRepository;
import com.auth.auth_app.service.IOnBoardingClientService;
import com.auth.auth_app.service.IRefreshTokenService;
import com.auth.auth_app.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OnBoardingClientServiceImp implements IOnBoardingClientService {

    private final AuthUserRepository authUserRepository;
    private final RealmRepository realmRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisteredClientRepository registeredClientRepository;
    private final AuthUtil authUtil;
    private final IRefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public OnboardingResponse onboard(OnboardingRequest request) {

        if (authUserRepository.findByEmail(request.email()).isPresent()){
            throw new RuntimeException("Email already exists : " + request.email());
        }

        if (realmRepository.existsByRealmName(request.realmName())){
            throw new RuntimeException("Realm name already exists : " + request.realmName());
        }

        Role userRole = roleRepository.findByNameAndRealmIsNull("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("Role_CLIENT not seeded"));

        AuthUser owner = AuthUser.builder()
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        owner = authUserRepository.save(owner);

        Realm realm = Realm.builder()
                .realmName(request.realmName())
                .displayName(request.appName())
                .enabled(true)
                .owner(owner)
                .build();

        realm = realmRepository.save(realm);

        owner.setMemberRealm(realm);
        authUserRepository.save(owner);

        String clientId = UUID.randomUUID().toString();
        String plainSecret = UUID.randomUUID().toString();
        String encodedSecret = passwordEncoder.encode(plainSecret);

        RegisteredClient registeredClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(encodedSecret)
                .clientName(request.appName())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri(request.redirectUri())
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .reuseRefreshTokens(false)
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .build())
                .build();

        registeredClientRepository.save(registeredClient);

        String baseUrl = "http://localhost:8080/api/" + request.realmName();

        String accessToken = authUtil.generateJWTToken(owner);

        String refreshToken = refreshTokenService.createRefreshToken(owner.getUserId()).getToken();

        return new OnboardingResponse(
                "Onboarding successful! Save your clientSecret — it will not be shown again.",
                request.email(),
                request.realmName(),
                clientId,
                plainSecret,
                accessToken,
                refreshToken,
                baseUrl + "/.well-known/openid-configuration",
                baseUrl + "/protocol/openid-connect/token"
        );
    }
}
