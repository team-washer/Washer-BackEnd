package com.washer.Things.domain.user.repository;

import com.washer.Things.domain.user.entity.User;
import com.washer.Things.global.entity.UserCredential;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    boolean existsUserByEmail(String email);

    @Query("SELECT new com.washer.Things.global.entity.UserCredential(u.id, u.email, u.password) FROM User u WHERE u.id = :userId")
    Optional<UserCredential> findCredentialById(Long userId);
}