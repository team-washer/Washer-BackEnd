package com.washer.Things.domain.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PwChangeRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String code;
}
