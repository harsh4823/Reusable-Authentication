package com.auth.auth_app.service;

import com.auth.auth_app.model.RealmRequest;
import com.auth.auth_app.model.RealmResponse;

import java.util.List;

public interface IRealmService {
    RealmResponse createRealm(RealmRequest request,String ownerEmail);
    RealmResponse getRealmByName(String realmName);
    List<RealmResponse> getAllRealms();
    RealmResponse updateRealm(String realmName, RealmRequest request);
    void deleteRealm(String realmName);
}
