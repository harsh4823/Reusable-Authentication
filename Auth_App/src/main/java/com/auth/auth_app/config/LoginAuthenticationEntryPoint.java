package com.auth.auth_app.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class LoginAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String FRONTEND_LOGIN_URL = "http://localhost:5173/login";

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        String originalUrl = request.getRequestURL().toString();

        if (request.getQueryString() != null) {
            originalUrl += "?" + request.getQueryString();
        }

        String redirectUrl = UriComponentsBuilder
                .fromUriString(FRONTEND_LOGIN_URL)
                .queryParam("continue", URLEncoder.encode(originalUrl, StandardCharsets.UTF_8))
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}