package com.washer.Things.global.util;

import com.washer.Things.domain.auth.presentation.dto.response.UserResponse;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.domain.user.repository.UserRepository;
import com.washer.Things.global.exception.HttpException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserUtil {
    private final UserRepository userRepository;

    @Transactional
    public UserResponse getUserInfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."));

        return UserResponse.builder()
                .id(user.getId().toString())
                .name(user.getName())
                .roomNumber(user.getRoom().getName())
                .gender(user.getGender().name().toLowerCase())
                .restrictedUntil(user.getRestrictedUntil().toString())
                .restrictionReason(user.getRestrictionReason())
                .build();
    }
}
