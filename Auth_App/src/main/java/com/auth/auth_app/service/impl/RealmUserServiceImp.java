package com.auth.auth_app.service.impl;

import com.auth.auth_app.Exception.ResourceNotFoundException;
import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.Realm;
import com.auth.auth_app.entity.Role;
import com.auth.auth_app.model.RealmUserResponse;
import com.auth.auth_app.model.RealmUserUpdateRequest;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.repository.RealmRepository;
import com.auth.auth_app.repository.RoleRepository;
import com.auth.auth_app.service.IRealmUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RealmUserServiceImp implements IRealmUserService {

    private final RealmRepository realmRepository;
    private final AuthUserRepository authUserRepository;
    private final RoleRepository roleRepository;

    @Override
    public Page<RealmUserResponse> getUsersInRealm(String realmName, Pageable pageable) {
        Realm realm = findRealm(realmName);

        Page<AuthUser> realmUsers = authUserRepository.findByMemberRealm(realm,pageable);

        return realmUsers.map(this::buildResponse);
    }

    @Override
    public RealmUserResponse getUserInRealm(String realmName, Long userId) {
        Realm realm = findRealm(realmName);
        AuthUser authUser = findUserInRealm(realm,userId);
        return buildResponse(authUser);
    }


    @Override
    @Transactional
    public RealmUserResponse updateUserInRealm(String realmName, Long userId, RealmUserUpdateRequest request) {
        Realm realm = findRealm(realmName);
        AuthUser authUser = findUserInRealm(realm,userId);

        authUser.setName(request.name());
        authUser.setEnabled(request.enabled());

        if (request.roles() != null){
            Set<Role> roles = request.roles().stream()
                    .map(roleName ->
                            roleRepository.findByNameAndRealm(roleName,realm)
                                    .orElseGet(() -> roleRepository.findByNameAndRealmIsNull(roleName)
                                            .orElseThrow(() -> new ResourceNotFoundException("Role","name",roleName)))
                    ).collect(Collectors.toSet());
            authUser.setRoles(roles);
        }

        return buildResponse(authUser);
    }

    @Override
    @Transactional
    public void removeUserFromRealm(String realmName, Long userId) {
        Realm realm = findRealm(realmName);
        AuthUser authUser = findUserInRealm(realm,userId);

        authUser.setMemberRealm(null);
        authUserRepository.save(authUser);
    }

    @Override
    @Transactional
    public RealmUserResponse assignRoleToUser(String realmName, Long userId, String roleName) {
        Realm realm = findRealm(realmName);
        AuthUser authUser = findUserInRealm(realm,userId);

        Role role = roleRepository.findByNameAndRealm(roleName,realm)
                .orElseGet(() -> roleRepository.findByNameAndRealmIsNull(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role","name",roleName)));
        authUser.getRoles().add(role);
        return buildResponse(authUser);
    }

    @Override
    @Transactional
    public RealmUserResponse removeRoleFromUser(String realmName, Long userId, String roleName) {
        Realm realm = findRealm(realmName);
        AuthUser authUser = findUserInRealm(realm,userId);

        authUser.getRoles().removeIf(role -> role.getName().equals(roleName));
        return buildResponse(authUser);
    }

    private Realm findRealm(String realmName) {
        return realmRepository.findByRealmName(realmName)
                .orElseThrow(() -> new ResourceNotFoundException("Realm", "realmName", realmName));
    }

    private AuthUser findUserInRealm(Realm realm, Long userId) {
        return realm.getMembers().stream()
                .filter(user -> user.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User","UserId",userId.toString()));
    }

    private RealmUserResponse buildResponse(AuthUser authUser) {
        Set<String> roles = authUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new RealmUserResponse(
                authUser.getUserId(),
                authUser.getEmail(),
                authUser.getName(),
                authUser.getImage(),
                authUser.isEnabled(),
                roles,
                authUser.getCreatedAt()
        );
    }
}
