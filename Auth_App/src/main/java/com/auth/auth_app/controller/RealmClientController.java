package com.auth.auth_app.controller;

import com.auth.auth_app.model.ClientRegistrationRequest;
import com.auth.auth_app.model.ClientRegistrationResponse;
import com.auth.auth_app.service.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/realms/{realmName}/clients")
@RequiredArgsConstructor
public class RealmClientController {

    private final IAdminService adminService;

    @PostMapping
    @PreAuthorize("@realmOwnerShipGuard.isOwnerOrAdmin(#realmName)")
    public ResponseEntity<ClientRegistrationResponse> registerClient(
            @PathVariable String realmName,
            @RequestBody ClientRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.registerClient(realmName, request));
    }

    @GetMapping
    @PreAuthorize("@realmOwnerShipGuard.isOwnerOrAdmin(#realmName)")
    public ResponseEntity<List<ClientRegistrationResponse>> getClients(
            @PathVariable String realmName) {
        return ResponseEntity.ok(adminService.getClientsByRealm(realmName));
    }

    @GetMapping("/{clientId}")
    @PreAuthorize("@realmOwnerShipGuard.isOwnerOrAdmin(#realmName)")
    public ResponseEntity<ClientRegistrationResponse> getClient(
            @PathVariable String realmName,
            @PathVariable String clientId) {
        return ResponseEntity.ok(adminService.getClient(clientId));
    }

    @PutMapping("/{clientId}")
    @PreAuthorize("@realmOwnerShipGuard.isOwnerOrAdmin(#realmName)")
    public ResponseEntity<ClientRegistrationResponse> updateClient(
            @PathVariable String realmName,
            @PathVariable String clientId,
            @RequestBody ClientRegistrationRequest request) {
        return ResponseEntity.ok(adminService.updateClient(clientId, request));
    }

    @DeleteMapping("/{clientId}")
    @PreAuthorize("@realmOwnerShipGuard.isOwnerOrAdmin(#realmName)")
    public ResponseEntity<Void> deleteClient(
            @PathVariable String realmName,
            @PathVariable String clientId) {
        adminService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clientId}/regenerate-secret")
    @PreAuthorize("@realmOwnerShipGuard.isOwnerOrAdmin(#realmName)")
    public ResponseEntity<ClientRegistrationResponse> regenerateSecret(
            @PathVariable String realmName,
            @PathVariable String clientId) {
        return ResponseEntity.ok(adminService.regenerateClientSecret(clientId));
    }
}