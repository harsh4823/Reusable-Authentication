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

        discovery.put("issuer", baseUrl);

        discovery.put("authorization_endpoint", baseUrl + "/protocol/openid-connect/auth");

        discovery.put("token_endpoint", baseUrl + "/protocol/openid-connect/token");

        discovery.put("userinfo_endpoint", baseUrl + "/protocol/openid-connect/userinfo");

        discovery.put("introspection_endpoint", baseUrl + "/protocol/openid-connect/token/introspect");

        discovery.put("end_session_endpoint", baseUrl + "/protocol/openid-connect/logout");

        discovery.put("jwks_uri", baseUrl + "/protocol/openid-connect/certs");

        discovery.put("grant_types_supported",
                List.of("authorization_code", "refresh_token", "client_credentials"));

        discovery.put("id_token_signing_alg_values_supported", List.of("RS256"));

        discovery.put("scopes_supported", List.of("openid", "profile", "email"));

        discovery.put("claims_supported",
                List.of("sub", "email", "name", "roles", "realm", "iat", "exp"));

        return ResponseEntity.ok(discovery);
    }


    @GetMapping("/{realm}/protocol/openid-connect/certs")
    public ResponseEntity<Map<String, Object>> jwks(@PathVariable String realm) {
        PublicKey publicKey = authUtil.getPublicKey();

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
