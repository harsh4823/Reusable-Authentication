package com.auth.auth_app.service;

import com.auth.auth_app.Exception.Oauth2MissingEmailException;
import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.ProviderType;
import com.auth.auth_app.model.AuthUserDto;
import com.auth.auth_app.model.LoginRequest;
import com.auth.auth_app.model.LoginResponse;
import com.auth.auth_app.model.OAuth2UserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IAuthService {

    LoginResponse authenticateAndGenerateToken(LoginRequest loginRequest);

    void registerUser(AuthUserDto authUserDto, MultipartFile profilePicture) throws IOException;

    AuthUser registerUser(OAuth2UserInfo oAuth2UserInfo, ProviderType providerType) throws IOException;

    LoginResponse handleOAuth2LoginRequest(OAuth2User user, String registrationId) throws IOException, Oauth2MissingEmailException;

    void logoutFromSingleDevice(Long userId,String jwt,String refreshToken);

    void logoutFromAllDevices(Long userId);
}
