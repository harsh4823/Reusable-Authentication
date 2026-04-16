package com.auth.auth_app.service.impl;

import com.auth.auth_app.Exception.ResourceNotFoundException;
import com.auth.auth_app.entity.Realm;
import com.auth.auth_app.entity.Role;
import com.auth.auth_app.model.RoleRequest;
import com.auth.auth_app.model.RoleResponse;
import com.auth.auth_app.repository.RealmRepository;
import com.auth.auth_app.repository.RoleRepository;
import com.auth.auth_app.service.IRealmRoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RealmRoleServiceImp implements IRealmRoleService {

    private final RealmRepository realmRepository;
    private final RoleRepository roleRepository;

    @Override
    public RoleResponse createRole(String realmName, RoleRequest roleRequest) {
        Realm realm = findRealmByName(realmName);

        if (roleRepository.existsByNameAndRealm(roleRequest.name(),realm)){
            throw new RuntimeException("Role : " + roleRequest.name() +  " already exists in realm : " + realmName);
        }

        Role role = Role.builder()
                .id(UUID.randomUUID())
                .name(roleRequest.name())
                .realm(realm)
                .build();

        return buildResponse(role);
    }

    @Override
    public List<RoleResponse> getAllRolesInRealm(String realmName) {
        Realm realm = findRealmByName(realmName);
        List<Role> roles = roleRepository.findByRealm(realm);
        return roles.stream().map(this::buildResponse).toList();
    }

    @Override
    @Transactional
    public void deleteRole(String realmName, String roleName) {
        Realm realm = findRealmByName(realmName);
        Role role = roleRepository.findByNameAndRealm(roleName,realm)
                .orElseThrow(() -> new ResourceNotFoundException("Role","name",roleName));

        realm.getMembers().forEach(user -> user.getRoles().remove(role));
        roleRepository.delete(role);
    }

    private Realm findRealmByName(String realmName) {
        return realmRepository.findByRealmName(realmName)
                .orElseThrow(() -> new ResourceNotFoundException("Realm","realmName", realmName));
    }

    private RoleResponse buildResponse(Role role) {
        return new RoleResponse(
                role.getId().toString(),
                role.getName(),
                (role.getRealm() != null) ? role.getRealm().getRealmName() : null
        );
    }
}
