package com.auth.auth_app.service;

import com.auth.auth_app.model.RealmLoginRequest;
import com.auth.auth_app.model.RealmLoginResponse;
import com.auth.auth_app.model.RealmRegisterRequest;

public interface IRealmAuthService {
    RealmLoginResponse login(String realmName, RealmLoginRequest request);
    RealmLoginResponse register(String realmName, RealmRegisterRequest request);
}
