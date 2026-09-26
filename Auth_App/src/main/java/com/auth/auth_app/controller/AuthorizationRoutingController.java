package com.auth.auth_app.controller;

import com.auth.auth_app.entity.Realm;
import com.auth.auth_app.repository.RealmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/realms/{realmName}/protocol/openid-connect")
@RequiredArgsConstructor
public class AuthorizationRoutingController {

    private final RealmRepository realmRepository;

    @GetMapping("/auth")
    public String routeToLogin(
            @PathVariable String realmName,
            @RequestParam String clientId,
            @RequestParam String responseType,
            @RequestParam String redirectUri,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String scope
    ) {
        Realm realm = realmRepository.findByRealmName(realmName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid realm: " + realmName));

        if (!realm.isEnabled()) {
            throw new IllegalStateException("Realm is disabled");
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .queryParam("client_id", clientId)
                .queryParam("response_type", responseType)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("realm", realmName);

        if (state != null) uriBuilder.queryParam("state", state);
        if (scope != null) uriBuilder.queryParam("scope", scope);

        String queryParams = uriBuilder.build().encode().toUriString();

        return switch (realm.getAuthMethod()) {
            case EMAIL_PASSWORD -> "redirect:/login/password" + queryParams;
            case EMAIL_OTP -> "redirect:/login/otp" + queryParams;
            case QR_CODE -> "redirect:/login/qr" + queryParams;
        };
    }
}