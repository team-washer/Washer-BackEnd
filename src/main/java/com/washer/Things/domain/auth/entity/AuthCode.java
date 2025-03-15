package com.washer.Things.domain.auth.entity;

import com.washer.Things.domain.auth.presentation.dto.request.AuthCodeRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Table(name = "auth_code")
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AuthCode {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID code;

    @Column(name = "email", unique = true)
    String email;

    public AuthCode(AuthCodeRequest request) {
        this.email = request.getEmail();
    }
}
