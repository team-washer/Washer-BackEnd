package com.washer.Things.domain.auth.repository;

import com.washer.Things.domain.auth.entity.AuthCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthCodeRepository extends JpaRepository<AuthCode, Long> {
    AuthCode findByEmail(String email);
    void deleteByEmail(String email);
}
