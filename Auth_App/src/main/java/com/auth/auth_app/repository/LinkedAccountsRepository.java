package com.auth.auth_app.repository;

import com.auth.auth_app.entity.LinkedAccounts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkedAccountsRepository extends JpaRepository<LinkedAccounts, Long> {
}