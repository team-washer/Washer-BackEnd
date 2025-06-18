package com.washer.Things.domain.fcmToken.entity;

import com.washer.Things.domain.fcmToken.entity.enums.PlatformType;
import com.washer.Things.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "fcm_token")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FcmToken {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String token;

    @Enumerated(EnumType.STRING)
    private PlatformType platform;
}
