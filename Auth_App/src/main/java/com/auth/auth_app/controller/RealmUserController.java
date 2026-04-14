package com.auth.auth_app.controller;

import com.auth.auth_app.model.RealmUserResponse;
import com.auth.auth_app.model.RealmUserUpdateRequest;
import com.auth.auth_app.service.IRealmUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/realms/{realmName}/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class RealmUserController {

    private final IRealmUserService realmUserService;

    @GetMapping
    public ResponseEntity<Page<RealmUserResponse>> getAllUsersInRealm(
            @PathVariable String realmName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(realmUserService.getUsersInRealm(
                realmName, PageRequest.of(page,size)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<RealmUserResponse> getUserInRealm(
            @PathVariable String realmName,
            @PathVariable Long userId
    ){
        return ResponseEntity.ok(realmUserService.getUserInRealm(realmName,userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<RealmUserResponse> updateUserInRealm(
            @PathVariable String realmName,
            @PathVariable Long userId,
            @RequestBody RealmUserUpdateRequest request
            ){
        return ResponseEntity.ok(realmUserService.updateUserInRealm(realmName,userId,request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeUserInRealm(
            @PathVariable String realmName,
            @PathVariable Long userId
    ){
        realmUserService.removeUserFromRealm(realmName,userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles/{roleName}")
    public ResponseEntity<RealmUserResponse> assignRoleToUserInRealm(
            @PathVariable String realmName,
            @PathVariable Long userId,
            @PathVariable String roleName
    ){
        return ResponseEntity.ok(realmUserService.assignRoleToUser(realmName,userId,roleName));
    }

    @DeleteMapping("/{userId}/roles/{roleName}")
    public ResponseEntity<RealmUserResponse> removeRoleFromUserInRealm(
            @PathVariable String realmName,
            @PathVariable Long userId,
            @PathVariable String roleName
    ){
        return ResponseEntity.ok(realmUserService.removeRoleFromUser(realmName,userId,roleName));
    }

}
