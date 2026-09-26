package com.auth.auth_app.strategy;

import com.auth.auth_app.entity.AuthMethod;
import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.Realm;
import com.auth.auth_app.entity.Role;
import com.auth.auth_app.model.RealmLoginRequest;
import com.auth.auth_app.model.RealmRegisterRequest;

public interface IRealmAuthStrategy {
    boolean supports(AuthMethod authMethod);
    AuthUser authenticate(RealmLoginRequest request);
    AuthUser registerNewUser(RealmRegisterRequest request, Realm realm, Role defaultRole);
}