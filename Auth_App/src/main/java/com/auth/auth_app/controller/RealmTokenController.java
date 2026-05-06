package com.auth.auth_app.controller;

import com.auth.auth_app.model.RealmLoginRequest;
import com.auth.auth_app.model.RealmRegisterRequest;
import com.auth.auth_app.service.IRealmAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RealmTokenController {

    private final IRealmAuthService realmAuthService;

    @PostMapping("/{realm}/protocol/openid-connect/token")
    public ResponseEntity<?> login(
            @PathVariable String realm,
            @Valid @RequestBody RealmLoginRequest request) {
        try {
            return ResponseEntity.ok(realmAuthService.login(realm, request));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{realm}/protocol/openid-connect/register")
    public ResponseEntity<?> register(
            @PathVariable String realm,
            @Valid @RequestBody RealmRegisterRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(realmAuthService.register(realm, request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}