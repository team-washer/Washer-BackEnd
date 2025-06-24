package com.washer.Things.domain.user.repository;

import com.washer.Things.domain.room.entity.Room;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.domain.user.entity.enums.Gender;
import com.washer.Things.global.entity.UserCredential;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findAllByRoom(Room room);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    boolean existsUserByEmail(String email);

    @Query("SELECT new com.washer.Things.global.entity.UserCredential(u.id, u.email, u.password) FROM User u WHERE u.id = :userId")
    Optional<UserCredential> findCredentialById(Long userId);

    @Query("SELECT u FROM User u " +
            "WHERE (:name IS NULL OR u.name LIKE CONCAT('%', :name, '%')) " +
            "AND (:gender IS NULL OR u.gender = :gender) " +
            "AND (:floor IS NULL OR (u.room IS NOT NULL AND FUNCTION('LEFT', u.room.name, 1) = :floor))")
    List<User> findByOptionalFilters(
            @Param("name") String name,
            @Param("gender") Gender gender,
            @Param("floor") String floor);
}