package com.washer.Things.domain.auth.service.impl;

import com.washer.Things.domain.auth.service.SignupService;
import com.washer.Things.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignupServiceImpl implements SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if(userRepository.existsUserByEmail(request.getEmail()))
            throw new HttpException(HttpStatus.BAD_REQUEST, "이미 해당 이름을 사용하는 멤버가 존재합니다.");
        User user = User.builder()
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .gender(request.getGender())
                .schoolNumber(request.getSchoolNumber())
                .roles(List.of(Role.ROLE_USER))
                .build();
        userRepository.save(user);
        return null;
    }
}
