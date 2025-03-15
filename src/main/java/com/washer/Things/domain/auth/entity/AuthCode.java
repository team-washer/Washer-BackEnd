package com.washer.Things.domain.auth.entity;

import com.washer.Things.domain.auth.presentation.dto.request.AuthCodeRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Table(name = "auth_code")
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AuthCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "code", nullable = false, length = 5)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public AuthCode(AuthCodeRequest request) {
        this.email = request.getEmail();
        this.code = generateAuthCode();
        this.expiresAt = LocalDateTime.now().plusMinutes(3); // 3분 후 만료
    }

    private String generateAuthCode() {
        Random random = new Random();
        return String.format("%05d", random.nextInt(100000)); // 5자리 숫자 생성
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
