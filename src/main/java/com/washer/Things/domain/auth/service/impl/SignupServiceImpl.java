package com.washer.Things.domain.auth.service.impl;
import com.washer.Things.domain.room.entity.Room;
import com.washer.Things.domain.auth.presentation.dto.request.SignupRequest;
import com.washer.Things.domain.auth.presentation.dto.response.TokenResponse;
import com.washer.Things.domain.auth.service.SignupService;
import com.washer.Things.domain.room.repository.RoomRepository;
import com.washer.Things.domain.user.entity.Role;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.domain.user.repository.UserRepository;
import com.washer.Things.global.exception.HttpException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignupServiceImpl implements SignupService {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if(userRepository.existsUserByEmail(request.getEmail()))
            throw new HttpException(HttpStatus.BAD_REQUEST, "이미 해당 이름을 사용하는 멤버가 존재합니다.");

        Room room = roomRepository.findByName(request.getRoom())
                .orElseThrow(() -> new HttpException(HttpStatus.BAD_REQUEST, "존재하지 않는 방입니다."));

        User user = User.builder()
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .grade(request.getGrade())
                .classRoom(request.getClassRoom())
                .number(request.getNumber())
                .roles(List.of(Role.ROLE_USER))
                .gender(request.getGender())
                .room(room)
                .build();
        userRepository.save(user);
        return null;
    }
}
