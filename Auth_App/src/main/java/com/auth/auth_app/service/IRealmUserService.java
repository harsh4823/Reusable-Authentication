package com.auth.auth_app.service;

import com.auth.auth_app.model.RealmUserResponse;
import com.auth.auth_app.model.RealmUserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRealmUserService {

    Page<RealmUserResponse> getUsersInRealm(String realmName, Pageable pageable);

    RealmUserResponse getUserInRealm(String realmName, Long userId);

    RealmUserResponse updateUserInRealm(String realmName, Long userId, RealmUserUpdateRequest request);

    void removeUserFromRealm(String realmName, Long userId);

    RealmUserResponse assignRoleToUser(String realmName, Long userId, String roleName);

    RealmUserResponse removeRoleFromUser(String realmName, Long userId, String roleName);
}
