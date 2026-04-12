package com.auth.auth_app.controller;

import com.auth.auth_app.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OidcDiscoveryController {

    private final AuthUtil authUtil;

    @GetMapping("/{realm}/.well-known/openid-configuration")
    public ResponseEntity<Map<String, Object>> discoveryDocument(
            @PathVariable String realm,
            @RequestHeader(value = "Host", defaultValue = "localhost:8080") String host) {

        String baseUrl = "http://" + host + "/api/" + realm;

        Map<String, Object> discovery = new LinkedHashMap<>();

        // The issuer MUST match the `iss` claim in your JWTs
        discovery.put("issuer", baseUrl);

        // Where clients redirect users to log in
        discovery.put("authorization_endpoint", baseUrl + "/protocol/openid-connect/auth");

        // Where clients exchange auth codes or refresh tokens for JWTs
        discovery.put("token_endpoint", baseUrl + "/protocol/openid-connect/token");

        // Where clients fetch the current user's profile info
        discovery.put("userinfo_endpoint", baseUrl + "/protocol/openid-connect/userinfo");

        // Where clients can verify a token is still valid (not revoked)
        discovery.put("introspection_endpoint", baseUrl + "/protocol/openid-connect/token/introspect");

        // Where clients can invalidate tokens
        discovery.put("end_session_endpoint", baseUrl + "/protocol/openid-connect/logout");

        // Public keys used to verify JWT signatures — clients cache these
        discovery.put("jwks_uri", baseUrl + "/protocol/openid-connect/certs");

        // Which OAuth2 flows are supported
        discovery.put("grant_types_supported",
                List.of("authorization_code", "refresh_token", "client_credentials"));

        // Which JWT signing algorithms are supported
        discovery.put("id_token_signing_alg_values_supported", List.of("RS256"));

        // Which scopes exist
        discovery.put("scopes_supported", List.of("openid", "profile", "email"));

        // Standard claims in the token
        discovery.put("claims_supported",
                List.of("sub", "email", "name", "roles", "realm", "iat", "exp"));

        return ResponseEntity.ok(discovery);
    }

    /**
     * JWKS (JSON Web Key Set) Endpoint
     * Standard endpoint: GET /{realm}/protocol/openid-connect/certs
     *
     * WHY: Resource servers (your APIs) call this on startup to get the public key
     * so they can verify JWT signatures WITHOUT calling your auth server on every request.
     * This is the stateless JWT verification flow.
     */
    @GetMapping("/{realm}/protocol/openid-connect/certs")
    public ResponseEntity<Map<String, Object>> jwks(@PathVariable String realm) {
        PublicKey publicKey = authUtil.getPublicKey();

        // Encode the raw public key bytes as Base64 URL-safe for the JWKS format
        String base64Key = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(publicKey.getEncoded());

        Map<String, Object> key = new LinkedHashMap<>();
        key.put("kty", "RSA");          // Key type
        key.put("use", "sig");          // Usage: signature verification
        key.put("alg", "RS256");        // Algorithm
        key.put("kid", "auth-app-key"); // Key ID — clients use this to pick the right key
        key.put("n", base64Key);        // The actual public key material

        Map<String, Object> jwks = Map.of("keys", List.of(key));
        return ResponseEntity.ok(jwks);
    }
}
