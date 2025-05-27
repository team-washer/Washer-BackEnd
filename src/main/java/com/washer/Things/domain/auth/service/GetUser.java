package com.washer.Things.domain.auth.service;

import com.washer.Things.domain.auth.presentation.dto.response.UserResponse;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GetUser {
    private final UserRepository userRepository;

    @Transactional
    public UserResponse getUserInfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."));

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
