// Auth_App/src/main/java/com/auth/auth_app/repository/RoleRepository.java
package com.auth.auth_app.repository;

import com.auth.auth_app.entity.Realm;
import com.auth.auth_app.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByNameAndRealmIsNull(String name);

    Optional<Role> findByNameAndRealm(String name, Realm realm);

    List<Role> findByRealm(Realm realm);

    boolean existsByNameAndRealmIsNull(String name);

    boolean existsByNameAndRealm(String name, Realm realm);
}