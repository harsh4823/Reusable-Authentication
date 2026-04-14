package com.auth.auth_app.repository;

import com.auth.auth_app.entity.RsaKeyPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RsaKeyPairRepository extends JpaRepository<RsaKeyPair,String> {
}