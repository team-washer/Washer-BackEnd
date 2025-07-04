package com.washer.Things.domain.fcmToken.repository;

import com.washer.Things.domain.fcmToken.entity.FcmToken;
import com.washer.Things.domain.fcmToken.entity.enums.PlatformType;
import com.washer.Things.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByUserAndPlatform(User user, PlatformType platform);
    @Query("SELECT ft FROM FcmToken ft WHERE ft.id IN (" +
            "SELECT MAX(ft2.id) FROM FcmToken ft2 WHERE ft2.user IN :users GROUP BY ft2.user)")
    List<FcmToken> findLatestTokenPerUser(List<User> users);
    Optional<FcmToken> findTopByUserOrderByIdDesc(User user);
}
