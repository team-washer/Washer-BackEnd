package com.washer.Things.domain.auth.entity;

import com.washer.Things.domain.auth.entity.enums.VerifyCodeType;
import com.washer.Things.domain.auth.presentation.dto.request.AuthCodeRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Table(name = "auth_code")
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AuthCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email")
    private String email;

    @Column(name = "code", length = 5)
    private String code;

    @Column(name = "auth_expires_at")
    private LocalDateTime authCodeExpiresAt;

    @Enumerated(EnumType.STRING)
    private VerifyCodeType type;

    public AuthCode(AuthCodeRequest emailCodeRequest, VerifyCodeType type) {
        this.email = emailCodeRequest.getEmail();
        this.type = type;
        this.code = generateCode();
        this.authCodeExpiresAt = LocalDateTime.now().plusMinutes(3);
    }

    private String generateCode() {
        Random random = new Random();
        return String.format("%05d", random.nextInt(100000));
    }

    public boolean isAuthCodeExpired() {
        return LocalDateTime.now().isAfter(this.authCodeExpiresAt);
    }
}
