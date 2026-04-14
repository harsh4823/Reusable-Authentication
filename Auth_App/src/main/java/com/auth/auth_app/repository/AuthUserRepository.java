package com.auth.auth_app.repository;

import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.Realm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByEmail(String email);

    Page<AuthUser> findByMemberRealm(Realm memberRealm, Pageable pageable);
}