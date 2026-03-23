package com.auth.auth_app.util;

import com.auth.auth_app.constant.ApplicationConstant;
import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.LinkedAccounts;
import com.auth.auth_app.entity.ProviderType;
import com.auth.auth_app.entity.Role;
import com.auth.auth_app.repository.LinkedAccountsRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final Environment env;
    private final LinkedAccountsRepository linkedAccountsRepository;

    public String generateJWTToken(Authentication authentication){
        String secret = env.getProperty(ApplicationConstant.JWT_SECRET,ApplicationConstant.JWT_SECRET_DEFAULT_VALUE);
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder().issuer("Harsh").subject("JWT Token")
                .claim("email",authentication.getName())
                .claim("authorities",authentication.getAuthorities().stream().map(
                        GrantedAuthority::getAuthority
                ).collect(Collectors.joining(",")))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() +  900000 ))
                .signWith(secretKey).compact();
    }

    public String generateJWTToken(AuthUser authUser){
        String secret = env.getProperty(ApplicationConstant.JWT_SECRET,ApplicationConstant.JWT_SECRET_DEFAULT_VALUE);
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .issuer("Harsh")
                .subject("JWT Token")
                .claim("email",authUser.getEmail())
                .claim("authorities", "ROLE_USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() +  900000 ))
                .signWith(secretKey)
                .compact();
    }

    public ProviderType getProviderFromRegistrationId(String registrationId){
        return switch (registrationId.toLowerCase()){
            case "google" -> ProviderType.GOOGLE;
            case "facebook" -> ProviderType.FACEBOOK;
            case "github" -> ProviderType.GITHUB;
            default -> throw new IllegalArgumentException("Invalid registration id");
        };
    }

    public String determineProviderIdFromOAuth2User(OAuth2User oAuth2User,String registrationId) {
        String providerId = switch (registrationId.toLowerCase()){
            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("id").toString();
            default -> throw new IllegalArgumentException("Invalid registration id");
        };

        if (providerId == null || providerId.isBlank()){
            throw new IllegalArgumentException("Invalid registration id");
        }
        return providerId;
    }

    public void linkNewProvider(AuthUser existingUser, String providerId, ProviderType providerType) {
        LinkedAccounts newLink = new LinkedAccounts();
        newLink.setProviderId(providerId);
        newLink.setProviderType(providerType);
        newLink.setAuthUser(existingUser);
        existingUser.getLinkedAccounts().add(newLink);
        linkedAccountsRepository.save(newLink);
    }

    public String extractJwt(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        } else if (bearerToken != null) {
            return bearerToken;
        }
        return extractCookie(request, "jwt");
    }

    public String extractRefreshToken(HttpServletRequest request) {
        String refreshHeader = request.getHeader("Refresh-Token");
        if (refreshHeader != null) {
            return refreshHeader;
        }
        return extractCookie(request, "refreshToken");
    }

    public String extractCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(cookieName))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public void clearBrowserCookies(HttpServletResponse response) {
        Cookie clearJwtCookie = new Cookie("jwt", null);
        clearJwtCookie.setPath("/");
        clearJwtCookie.setHttpOnly(true);
        clearJwtCookie.setMaxAge(0);

        Cookie clearRefreshCookie = new Cookie("refreshToken", null);
        clearRefreshCookie.setPath("/api/auth/refresh");
        clearRefreshCookie.setHttpOnly(true);
        clearRefreshCookie.setMaxAge(0);

        response.addCookie(clearJwtCookie);
        response.addCookie(clearRefreshCookie);
    }

}
