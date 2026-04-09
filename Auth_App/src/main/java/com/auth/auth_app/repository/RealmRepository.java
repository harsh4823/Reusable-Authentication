package com.auth.auth_app.repository;

import com.auth.auth_app.entity.Realm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RealmRepository extends JpaRepository<Long, Realm> {
}
