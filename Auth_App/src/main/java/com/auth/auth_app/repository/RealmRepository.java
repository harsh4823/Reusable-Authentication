package com.auth.auth_app.repository;

import com.auth.auth_app.entity.Realm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RealmRepository extends JpaRepository<Realm, Long> {
    boolean existsByRealmName(String realmName);

    Optional<Realm> findByRealmName(String realmName);
}
