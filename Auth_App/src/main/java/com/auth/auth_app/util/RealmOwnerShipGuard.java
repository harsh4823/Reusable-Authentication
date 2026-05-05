package com.auth.auth_app.util;

import com.auth.auth_app.entity.Realm;
import com.auth.auth_app.repository.RealmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RealmOwnerShipGuard {

    private final RealmRepository realmRepository;

    public boolean isOwnerOrAdmin(String realmName){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        assert auth != null;
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        if (isSuperAdmin) return true;

        String email = (String) auth.getPrincipal();
        Realm realm = realmRepository.findByRealmName(realmName).orElse(null);

        if (realm == null) return false;

        return realm.getOwner().getEmail().equals(email);
    }
}
