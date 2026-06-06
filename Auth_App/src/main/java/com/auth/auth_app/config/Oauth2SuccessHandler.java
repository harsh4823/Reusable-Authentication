package com.auth.auth_app.config;

import com.auth.auth_app.Exception.Oauth2MissingEmailException;
import com.auth.auth_app.model.LoginResponse;
import com.auth.auth_app.service.IAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

    private final IAuthService authService;

    @Value("${app.frontend.base-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User user = token.getPrincipal();
        String registrationId = token.getAuthorizedClientRegistrationId();

        try {
            LoginResponse loginResponse = authService.handleOAuth2LoginRequest(user,registrationId);
            String jwt = loginResponse.accessToken();
            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true);     // Prevents JavaScript (XSS) from reading the token
            jwtCookie.setSecure(false);      // Set to true in production when using HTTPS
            jwtCookie.setPath("/");          // Available to all API endpoints
            jwtCookie.setMaxAge(900);      // Expiration in seconds (align with JWT expiration)
            response.addCookie(jwtCookie);

            String refreshToken = loginResponse.refreshToken();
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);       // ← refreshCookie, not jwtCookie
            refreshCookie.setSecure(false);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days in seconds
            response.addCookie(refreshCookie);

            response.sendRedirect(frontendUrl + "/oauth/success");
        } catch (Oauth2MissingEmailException e) {
            throw new RuntimeException(e);
        }
    }
}
