package com.auth.auth_app.service.impl;

import com.auth.auth_app.Exception.ResourceNotFoundException;
import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.Realm;
import com.auth.auth_app.entity.Role;
import com.auth.auth_app.model.RealmLoginRequest;
import com.auth.auth_app.model.RealmLoginResponse;
import com.auth.auth_app.model.RealmRegisterRequest;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.repository.RealmRepository;
import com.auth.auth_app.repository.RoleRepository;
import com.auth.auth_app.repository.TokenRepository;
import com.auth.auth_app.service.IRealmAuthService;
import com.auth.auth_app.service.IRefreshTokenService;
import com.auth.auth_app.util.AuthUtil;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RealmAuthServiceImp implements IRealmAuthService {

    private final AuthUserRepository authUserRepository;
    private final TokenRepository tokenRepository;
    private final RealmRepository realmRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtil authUtil;
    private final RoleRepository roleRepository;
    private final IRefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public RealmLoginResponse login(String realmName, RealmLoginRequest request) {
        Realm realm = findRealmByName(realmName);

        AuthUser authUser = authUserRepository.findByEmail(request.email())
                .orElseThrow(()-> new ResourceNotFoundException("AuthUser","email",request.email()));

        boolean isMember = authUser.getMemberRealm() != null
                && authUser.getMemberRealm().getRealmName().equals(realmName);

        boolean isOwner = realm.getOwner().getEmail().equals(request.email());
        boolean isAdmin = authUser.getRoles().stream()
                .anyMatch(r->r.getName().equals("ROLE_ADMIN"));

        if (!isMember && !isOwner && !isAdmin) {
            throw new BadCredentialsException("User doesn't have access to this resource");
        }

        if (!authUser.isEnabled()){
            throw new RuntimeException("User is disabled");
        }

        if (!passwordEncoder.matches(request.password(), authUser.getPassword())){
            throw new BadCredentialsException("Invalid password");
        }

        String accessToken = buildRealmAccessToken(authUser,realm);
        String refreshToken = refreshTokenService.createRefreshToken(authUser.getUserId()).getToken();
        tokenRepository.storeTokens(authUser.getUserId(), accessToken, refreshToken);

        return new RealmLoginResponse(accessToken,refreshToken,"Bearer",900L,realmName);
    }

    @Override
    public RealmLoginResponse register(String realmName, RealmRegisterRequest request) {
        Realm realm = findRealmByName(realmName);

        if (authUserRepository.findByEmail(request.email()).isPresent()) {
            throw new BadCredentialsException("Email already exists");
        }

        Role role = roleRepository.findByNameAndRealmIsNull("ROLE_USER")
                .orElseThrow(()-> new RuntimeException("Role_USER not seeded"));

        AuthUser authUser = AuthUser.builder()
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .memberRealm(realm)
                .roles(new HashSet<>(Set.of(role)))
                .build();

        authUser = authUserRepository.save(authUser);

        String accessToken = buildRealmAccessToken(authUser, realm);
        String refreshToken = refreshTokenService.createRefreshToken(authUser.getUserId()).getToken();
        tokenRepository.storeTokens(authUser.getUserId(), accessToken, refreshToken);

        return new RealmLoginResponse(accessToken, refreshToken, "Bearer", 900L, realmName);
    }

    private Realm findRealmByName(String realmName) {
        Realm realm = realmRepository.findByRealmName(realmName)
                .orElseThrow(()-> new ResourceNotFoundException("Realm","realmName",realmName));

        if (!realm.isEnabled()){
            throw new RuntimeException("Realm is disabled : "+ realmName);
        }
        return realm;
    }

    private String buildRealmAccessToken(AuthUser authUser,Realm realm){
        String authorities = authUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));

        if (authorities.isBlank()) authorities = "ROLE_USER";

        return Jwts.builder()
                .issuer("Harsh")
                .subject("JWT Token")
                .claim("email",authUser.getEmail())
                .claim("authorities",authorities)
                .claim("realm",realm.getRealmName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900_000))
                .signWith(authUtil.getPrivateKey(), Jwts.SIG.RS256)
                .compact();
    }
}
