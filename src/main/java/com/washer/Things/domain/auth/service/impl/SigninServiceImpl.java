package com.washer.Things.domain.auth.service.impl;

import com.washer.Things.domain.auth.presentation.dto.request.SigninRequest;
import com.washer.Things.domain.auth.presentation.dto.response.TokenResponse;
import com.washer.Things.domain.auth.service.SigninService;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.domain.user.repository.UserRepository;
import com.washer.Things.global.exception.HttpException;
import com.washer.Things.global.security.jwt.TokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SigninServiceImpl implements SigninService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    @Transactional
    public TokenResponse signin(SigninRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "없는 유저 입니다."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new HttpException(HttpStatus.UNAUTHORIZED, "비밀번호가 틀렸습니다.");
        return tokenProvider.generateTokenSet(user.getId(), user.getRoles().get(0));
    }
}
