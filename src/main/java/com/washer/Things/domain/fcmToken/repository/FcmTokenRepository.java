package com.washer.Things.domain.fcmToken.repository;

import com.washer.Things.domain.fcmToken.entity.FcmToken;
import com.washer.Things.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByUserAndToken(User user, String token);
    List<FcmToken> findAllByUserIn(List<User> users);
}
