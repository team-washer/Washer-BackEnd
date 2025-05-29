package com.washer.Things.global.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "smartthings_tokens")
public class SmartThingsToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId; // 사용자 식별용(로그인 유저 아이디)

    private String accessToken;

    private String refreshToken;

    private Instant tokenIssuedAt;

    private Instant accessTokenExpiresAt;
}
