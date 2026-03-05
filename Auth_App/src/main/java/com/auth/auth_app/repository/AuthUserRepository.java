package com.auth.auth_app.repository;

import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {

    Optional<AuthUser> findByEmail(String email);

    Optional<AuthUser> findByProviderIdAndProviderType(String providerId, ProviderType providerType);

}