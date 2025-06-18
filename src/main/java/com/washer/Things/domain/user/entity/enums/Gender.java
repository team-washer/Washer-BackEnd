package com.washer.Things.domain.user.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public enum Gender {
    male("남성"),
    female("여성");

    private final String permission;
}
