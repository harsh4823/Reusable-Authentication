package com.auth.auth_app.controller;

import com.auth.auth_app.model.RoleRequest;
import com.auth.auth_app.model.RoleResponse;
import com.auth.auth_app.service.IRealmRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/realms/{realmName}/roles")
@PreAuthorize("@realmOwnerShipGuard.isOwnerOrAdmin(#realmName)")
@RequiredArgsConstructor
public class RealmRoleController {

    private final IRealmRoleService realmRoleService;

    @PostMapping
    public ResponseEntity<RoleResponse> createRole(
            @PathVariable String realmName,
            @RequestBody RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(realmRoleService.createRole(realmName, request));
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getRoles(@PathVariable String realmName) {
        return ResponseEntity.ok(realmRoleService.getAllRolesInRealm(realmName));
    }

    @DeleteMapping("/{roleName}")
    public ResponseEntity<Void> deleteRole(
            @PathVariable String realmName,
            @PathVariable String roleName) {
        realmRoleService.deleteRole(realmName, roleName);
        return ResponseEntity.noContent().build();
    }
}