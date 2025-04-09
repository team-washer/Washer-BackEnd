package com.washer.Things.domain.auth.repository;
import com.washer.Things.domain.auth.entity.PasswordChangeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordChangeCodeRepository extends JpaRepository<PasswordChangeCode, Long> {
    PasswordChangeCode findByEmail(String email);
    void deleteByEmail(String email);
}
