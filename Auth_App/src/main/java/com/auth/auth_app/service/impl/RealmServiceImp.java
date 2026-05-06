package com.auth.auth_app.service.impl;

import com.auth.auth_app.Exception.ResourceNotFoundException;
import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.Realm;
import com.auth.auth_app.entity.Role;
import com.auth.auth_app.model.RealmRequest;
import com.auth.auth_app.model.RealmResponse;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.repository.RealmRepository;
import com.auth.auth_app.repository.RoleRepository;
import com.auth.auth_app.service.IRealmService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RealmServiceImp implements IRealmService {

    private final AuthUserRepository authUserRepository;
    private final RealmRepository realmRepository;
    private final RoleRepository roleRepository;

    @Transactional
    @Override
    public RealmResponse createRealm(RealmRequest request, String ownerEmail) {

        if (realmRepository.existsByRealmName(request.realmName())){
            throw new RuntimeException("Realm already exists : " + request.realmName());
        }

        AuthUser owner = authUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Owner","email",ownerEmail));

        Realm realm = Realm.builder()
                .realmName(request.realmName())
                .displayName(request.displayName())
                .enabled(request.enabled())
                .owner(owner)
                .build();
        Realm savedRealm = realmRepository.save(realm);

        boolean alreadyClient = owner.getRoles().stream()
                .anyMatch(r->r.getName().equals("ROLE_CLIENT"));

        if (!alreadyClient){
            Role clientRole = roleRepository.findByNameAndRealmIsNull("ROLE_Client")
                    .orElseThrow(()-> new RuntimeException("Role Client not seeded"));

            owner.getRoles().add(clientRole);
            authUserRepository.save(owner);
        }

        return buildResponse(savedRealm);
    }

    @Override
    public RealmResponse getRealmByName(String realmName) {
        Realm realm = realmRepository.findByRealmName(realmName)
                .orElseThrow(()-> new ResourceNotFoundException("Realm","realmName",realmName));
        return buildResponse(realm);
    }

    @Override
    public List<RealmResponse> getAllRealms() {
        return realmRepository.findAll()
                .stream().map(this::buildResponse)
                .toList();
    }

    @Transactional
    @Override
    public RealmResponse updateRealm(String realmName, RealmRequest request) {
        Realm realm = realmRepository.findByRealmName(realmName)
                .orElseThrow(()-> new ResourceNotFoundException("Realm","realmName",realmName));

        realm.setDisplayName(request.displayName());
        realm.setEnabled(request.enabled());
        return buildResponse(realmRepository.save(realm));
    }

    @Transactional
    @Override
    public void deleteRealm(String realmName) {
        Realm realm = realmRepository.findByRealmName(realmName)
                .orElseThrow(()-> new ResourceNotFoundException("Realm","realmName",realmName));
        realmRepository.delete(realm);
    }


    private RealmResponse buildResponse(Realm savedRealm) {
        return new RealmResponse(
                savedRealm.getRealmId(),
                savedRealm.getRealmName(),
                savedRealm.getDisplayName(),
                savedRealm.isEnabled(),
                savedRealm.getOwner().getEmail(),
                savedRealm.getCreatedAt()
        );
    }
}
