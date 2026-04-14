package com.auth.auth_app.service.impl;

import com.auth.auth_app.Exception.Oauth2MissingEmailException;
import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.LinkedAccounts;
import com.auth.auth_app.entity.ProviderType;
import com.auth.auth_app.entity.Role;
import com.auth.auth_app.model.AuthUserDto;
import com.auth.auth_app.model.LoginRequest;
import com.auth.auth_app.model.LoginResponse;
import com.auth.auth_app.model.OAuth2UserInfo;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.repository.LinkedAccountsRepository;
import com.auth.auth_app.repository.RoleRepository;
import com.auth.auth_app.repository.TokenRepository;
import com.auth.auth_app.service.IAuthService;
import com.auth.auth_app.service.ICloudinaryService;
import com.auth.auth_app.service.IRefreshTokenService;
import com.auth.auth_app.util.AuthUtil;
import com.auth.auth_app.util.IOAuth2UserInfoExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final Environment env;
    private final ICloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;
    private final AuthUserRepository authUserRepository;
    private final LinkedAccountsRepository linkedAccountsRepository;
    private final List<IOAuth2UserInfoExtractor> extractors;
    private final AuthUtil authUtil;
    private final IRefreshTokenService refreshTokenService;
    private final TokenRepository tokenRepository;
    private final RoleRepository roleRepository;

    @Override
    public LoginResponse authenticateAndGenerateToken(LoginRequest loginRequest) {
        // Step 1: Authenticate FIRST — throws BadCredentialsException if wrong password
        Authentication authentication = UsernamePasswordAuthenticationToken
                .unauthenticated(loginRequest.email(), loginRequest.password());
        Authentication authenticate = authenticationManager.authenticate(authentication);

        // Step 2: Only reach here if credentials are valid
        if (authenticate != null && authenticate.isAuthenticated()) {
            AuthUser authUser = authUserRepository.findByEmail(loginRequest.email())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            String jwt = authUtil.generateJWTToken(authenticate);
            String refreshToken = refreshTokenService.createRefreshToken(authUser.getUserId()).getToken();
            tokenRepository.storeTokens(authUser.getUserId(), jwt, refreshToken);

            return new LoginResponse(HttpStatus.OK.getReasonPhrase(), jwt, refreshToken);
        }
        throw new BadCredentialsException("Authentication failed");
    }

    @Override
    @Transactional
    public void registerUser(AuthUserDto authUserDto, MultipartFile profilePicture) throws IOException {
        Role userRole = roleRepository.findByNameAndRealmIsNull("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found in DB"));

        String hashPassword = passwordEncoder.encode(authUserDto.password());
            Map data = cloudinaryService.upload(profilePicture);
            AuthUser authUser = AuthUser.builder()
                    .name(authUserDto.name())
                    .password(hashPassword)
                    .email(authUserDto.email())
                    .image(data.get("url").toString())
                    .roles(new HashSet<>(Set.of(userRole)))
                    .build();

            authUserRepository.save(authUser);

    }

    @Override
    @Transactional
    public AuthUser registerUser(OAuth2UserInfo oAuth2UserInfo, ProviderType providerType) throws IOException {
        String imageUrl = null;
        if (oAuth2UserInfo.avatarUrl() != null){
            Map data = cloudinaryService.upload(oAuth2UserInfo.avatarUrl());
            imageUrl = data.get("url").toString();
        }

        Role userRole = roleRepository.findByNameAndRealmIsNull("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found in DB"));

        AuthUser newUser = AuthUser.builder()
                .email(oAuth2UserInfo.email())
                .password(null)
                .name(oAuth2UserInfo.name())
                .image(imageUrl)
                .linkedAccounts(new ArrayList<>())
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        newUser = authUserRepository.save(newUser);

        authUtil.linkNewProvider(newUser, oAuth2UserInfo.providerId(),  providerType);

        return newUser;
    }

    @Override
    @Transactional
    public LoginResponse handleOAuth2LoginRequest(OAuth2User user, String registrationId) throws IOException, Oauth2MissingEmailException {
        ProviderType providerType = authUtil.getProviderFromRegistrationId(registrationId);

        OAuth2UserInfo userInfo = extractors.stream()
            .filter(extractor -> extractor.supports(providerType.toString()))
            .findFirst()
            .orElseThrow(()-> new OAuth2AuthenticationException(("Sorry! Login with " + providerType + " is not supported yet.")))
            .extractUserInfo(user.getAttributes());

        LinkedAccounts linkedAccounts = linkedAccountsRepository.findByProviderIdAndProviderType(userInfo.providerId(), providerType).orElse(null);

        if (linkedAccounts!=null){
            AuthUser existingLinkedUser = linkedAccounts.getAuthUser();
            return generateAndStoreTokens(existingLinkedUser);
        }

        if (userInfo.email()==null || userInfo.email().isBlank()){
            throw new Oauth2MissingEmailException(providerType.name(),userInfo.providerId());
        }

        AuthUser emailAuthUser = authUserRepository.findByEmail(userInfo.email())
                .orElse(null);

        if (emailAuthUser!=null){
            authUtil.linkNewProvider(emailAuthUser,userInfo.providerId(),providerType);
            return generateAndStoreTokens(emailAuthUser);
        }
        AuthUser authUser = registerUser(userInfo,providerType);
        return generateAndStoreTokens(authUser);
    }

    @Override
    public void logoutFromAllDevices(Long userId){
        tokenRepository.removeAllTokens(userId);
    }

    @Override
    public void logoutFromSingleDevice(Long userId,String jwt,String refreshToken){
        tokenRepository.removeSingleSession(userId,jwt,refreshToken);
    }

    private LoginResponse generateAndStoreTokens(AuthUser user) {
        String jwt = authUtil.generateJWTToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getUserId()).getToken();
        tokenRepository.storeTokens(user.getUserId(), jwt, refreshToken);
        return new LoginResponse(HttpStatus.OK.getReasonPhrase(), jwt, refreshToken);
    }
}
