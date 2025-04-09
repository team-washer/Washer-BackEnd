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

    @Column(name = "password_change_code", nullable = false, length = 5)
    private String passwordChangeCode;

    @Column(name = "auth_expires_at", nullable = false)
    private LocalDateTime authCodeExpiresAt;

    @Column(name = "password_change_expires_at", nullable = false)
    private LocalDateTime passwordChangeCodeExpiresAt;

    public AuthCode(AuthCodeRequest request) {
        this.email = request.getEmail();
        this.code = generateCode();
        this.authCodeExpiresAt = LocalDateTime.now().plusMinutes(3);
    }
    public void PasswordChangeCode(AuthCodeRequest request) {
        this.email = request.getEmail();
        this.passwordChangeCode = generateCode();
        this.passwordChangeCodeExpiresAt = LocalDateTime.now().plusMinutes(3);
    }

    private String generateCode() {
        Random random = new Random();
        return String.format("%05d", random.nextInt(100000));
    }

    public boolean isAuthCodeExpired() {
        return LocalDateTime.now().isAfter(this.authCodeExpiresAt);
    }

    public boolean isPasswordChangeCodeExpired() {
        return LocalDateTime.now().isAfter(this.passwordChangeCodeExpiresAt);
    }
}
