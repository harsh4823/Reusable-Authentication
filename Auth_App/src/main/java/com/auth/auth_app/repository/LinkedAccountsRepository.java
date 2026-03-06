package com.auth.auth_app.repository;

import com.auth.auth_app.entity.LinkedAccounts;
import com.auth.auth_app.entity.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkedAccountsRepository extends JpaRepository<LinkedAccounts, Long> {

    Optional<LinkedAccounts> findByProviderIdAndProviderType(String s, ProviderType providerType);

}