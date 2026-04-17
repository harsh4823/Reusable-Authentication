package com.auth.auth_app.controller;

import com.auth.auth_app.model.RealmRequest;
import com.auth.auth_app.model.RealmResponse;
import com.auth.auth_app.service.IRealmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/admin/realms")
@RequiredArgsConstructor
public class RealmController {

    private final IRealmService realmService;

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

    @GetMapping("/{realmName}")
    public ResponseEntity<RealmResponse> getRealm(@PathVariable String realmName) {
        return ResponseEntity.ok(realmService.getRealmByName(realmName));
    }

    @PutMapping("/{realmName}")
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
