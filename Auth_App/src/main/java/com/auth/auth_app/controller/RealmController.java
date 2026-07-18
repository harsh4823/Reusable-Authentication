package com.auth.auth_app.controller;

import com.auth.auth_app.Exception.ResourceNotFoundException;
import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.model.RealmRequest;
import com.auth.auth_app.model.RealmResponse;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.repository.RealmRepository;
import com.auth.auth_app.service.IRealmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/admin/realms")
@RequiredArgsConstructor
public class RealmController {

    private final IRealmService realmService;
    private final AuthUserRepository authUserRepository;
    private final RealmRepository realmRepository;

    @PostMapping
    public ResponseEntity<RealmResponse> createRealm(@RequestBody RealmRequest request){
        String email = (String) Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getPrincipal();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(realmService.createRealm(request,email));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<RealmResponse>> getAllRealms() {
        return ResponseEntity.ok(realmService.getAllRealms());
    }

    // frontend/src/main/java/com/auth/auth_app/controller/RealmController.java

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<RealmResponse>> getMyRealms() {

        // 1. Securely extract the current user's identity from the context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        if (auth.getPrincipal() instanceof String) {
            email = (String) auth.getPrincipal();
        }

        // 2. Fetch the actual AuthUser entity
        String finalEmail = email;
        AuthUser authUser = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found","Email", finalEmail));

        // 3. ALGORITHM: Tenant Data Isolation
        // Fetch strictly the realms owned by this specific user
        List<RealmResponse> myRealms = realmRepository.findByOwner(authUser)
                .stream()
                .map(realm -> new RealmResponse(
                        realm.getRealmId(),
                        realm.getRealmName(),
                        realm.getDisplayName(),
                        realm.isEnabled(),
                        realm.getOwner().getEmail(),
                        realm.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(myRealms);
    }

    @GetMapping("/{realmName}")
    @PreAuthorize("@realmOwnerShipGuard.isOwnerOrAdmin(#realmName)")
    public ResponseEntity<RealmResponse> getRealm(@PathVariable String realmName) {
        return ResponseEntity.ok(realmService.getRealmByName(realmName));
    }

    @PutMapping("/{realmName}")
    @PreAuthorize("@realmOwnerShipGuard.isOwnerOrAdmin(#realmName)")
    public ResponseEntity<RealmResponse> updateRealm(@PathVariable String realmName,
                                                     @RequestBody RealmRequest request) {
        return ResponseEntity.ok(realmService.updateRealm(realmName, request));
    }

    @DeleteMapping("/{realmName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRealm(@PathVariable String realmName) {
        realmService.deleteRealm(realmName);
        return ResponseEntity.noContent().build();
    }
}
