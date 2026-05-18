package com.auth.auth_app.controller;

import com.auth.auth_app.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
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
        RSAPublicKey rsaPublicKey = (RSAPublicKey) authUtil.getPublicKey();

        String n = Base64.getUrlEncoder()
                .withoutPadding().encodeToString(toUnsignedBytes(rsaPublicKey.getModulus()));

        String e = Base64.getUrlEncoder()
                .withoutPadding().encodeToString(toUnsignedBytes(rsaPublicKey.getPublicExponent()));

        Map<String, Object> key = new LinkedHashMap<>();
        key.put("kty", "RSA");          // Key type
        key.put("use", "sig");          // Usage: signature verification
        key.put("alg", "RS256");        // Algorithm
        key.put("kid", "auth-app-key"); // Key ID — clients use this to pick the right key
        key.put("n", n);                // The actual public key material
        key.put("e", e);

        Map<String, Object> jwks = Map.of("keys", List.of(key));
        return ResponseEntity.ok(jwks);
    }

    private byte[] toUnsignedBytes(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes[0] == 0) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            return trimmed;
        }
        return bytes;
    }
}
