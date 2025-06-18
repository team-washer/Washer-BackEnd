package com.washer.Things.global.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserCredential {
    private final Long id;
    private final String email;
    private final String encodedPassword;
}