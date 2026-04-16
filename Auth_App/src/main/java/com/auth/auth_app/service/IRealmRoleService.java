package com.auth.auth_app.service;

import com.auth.auth_app.model.RoleRequest;
import com.auth.auth_app.model.RoleResponse;

import java.util.List;

public interface IRealmRoleService {

    RoleResponse createRole(String realmName , RoleRequest roleRequest);
    List<RoleResponse> getAllRolesInRealm(String realmName);
    void deleteRole(String realmName, String roleName);
}
