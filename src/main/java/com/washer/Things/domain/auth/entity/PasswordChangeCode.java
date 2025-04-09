package com.washer.Things.domain.auth.entity;

import com.washer.Things.domain.auth.presentation.dto.request.AuthCodeRequest;
import com.washer.Things.domain.auth.presentation.dto.request.PasswordChangeCodeRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Table(name = "password_change_code")
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_change_code", nullable = false, length = 5)
    private String passwordChangeCode;

    @Column(name = "password_change_expires_at", nullable = false)
    private LocalDateTime passwordChangeCodeExpiresAt;
    public PasswordChangeCode(PasswordChangeCodeRequest request) {
        this.email = request.getEmail();
        this.passwordChangeCode = generateCode();
        this.passwordChangeCodeExpiresAt = LocalDateTime.now().plusMinutes(3);
    }

    private String generateCode() {
        Random random = new Random();
        return String.format("%05d", random.nextInt(100000));
    }

    public boolean isPasswordChangeCodeExpired() {
        return LocalDateTime.now().isAfter(this.passwordChangeCodeExpiresAt);
    }
}
