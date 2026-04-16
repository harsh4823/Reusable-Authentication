package com.auth.auth_app.util;

import com.auth.auth_app.entity.*;
import com.auth.auth_app.repository.LinkedAccountsRepository;
import com.auth.auth_app.repository.RsaKeyPairRepository;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthUtil {

    private final LinkedAccountsRepository linkedAccountsRepository;
    private final RsaKeyPairRepository rsaKeyPairRepository;

    @Getter
    private PrivateKey privateKey;

    @Getter
    private PublicKey publicKey;

    private static final String KEY_ID = "main";

    @PostConstruct
    public void initKeys() {

        rsaKeyPairRepository.findById(KEY_ID).ifPresentOrElse(
                stored -> {
                    log.info("Loading RSA Key Pair from databse");

                    try {
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

                        byte[] publicKeyBytes = Base64.getDecoder().decode(stored.getPublicKey());
                        this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

                        byte[] privateKeyBytes = Base64.getDecoder().decode(stored.getPrivateKey());
                        this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

                        log.info("RSA Key Pair Loaded successfully from databse");
                    }catch (Exception e){
                        throw new RuntimeException("Failed to load RSA Key Pair from databse");
                    }
                },
                () -> {
                    log.info("No RSA Key Pair found in database , generating new one and saving to database.");
                    try {
                        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                        keyPairGenerator.initialize(2048);
                        KeyPair keyPair = keyPairGenerator.generateKeyPair();

                        this.privateKey = keyPair.getPrivate();
                        this.publicKey = keyPair.getPublic();

                        String encodedPrivateKey = Base64.getEncoder().encodeToString(this.privateKey.getEncoded());
                        String encodedPublicKey = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());

                        rsaKeyPairRepository.save(RsaKeyPair.builder()
                                        .pairId(KEY_ID)
                                        .publicKey(encodedPublicKey)
                                        .privateKey(encodedPrivateKey)
                                        .build());

                        log.info("RSA Key Pair Generated and Saved Successfully");
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to generate RSA Key Pair.");
                    }
                }
        );
    }

    public String generateJWTToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        if (authorities.isEmpty()) {
            authorities = "ROLE_USER";
        }

        return Jwts.builder()
                .issuer("Harsh")
                .subject("JWT Token")
                .claim("email", authentication.getName())
                .claim("authorities", authorities)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900000))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String generateJWTToken(AuthUser authUser){
        String authorities = authUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));

        if (authorities.isEmpty()){
            authorities = "ROLE_USER";
        }

        String realm = authUser.getMemberRealm() != null
                ? authUser.getMemberRealm().getRealmName()
                : "master";

        return Jwts.builder()
                .issuer("Harsh")
                .subject("JWT Token")
                .claim("email", authUser.getEmail())
                .claim("authorities", authorities)
                .claim("realm",realm)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900000))
                .signWith(privateKey, Jwts.SIG.RS256)
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
