package com.washer.Things.domain.auth.service.impl;
import com.washer.Things.domain.auth.entity.AuthCode;
import com.washer.Things.domain.auth.entity.enums.VerifyCodeType;
import com.washer.Things.domain.auth.presentation.dto.request.AuthCodeRequest;
import com.washer.Things.domain.auth.presentation.dto.request.EmailVerifyRequest;
import com.washer.Things.domain.auth.repository.AuthCodeRepository;
import com.washer.Things.domain.room.entity.Room;
import com.washer.Things.domain.auth.presentation.dto.request.SignupRequest;
import com.washer.Things.domain.auth.service.SignupService;
import com.washer.Things.domain.room.repository.RoomRepository;
import com.washer.Things.domain.user.entity.enums.Gender;
import com.washer.Things.domain.user.entity.enums.Role;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.domain.user.repository.UserRepository;
import com.washer.Things.global.exception.HttpException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupServiceImpl implements SignupService {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final AuthCodeRepository authCodeRepository;
    @Transactional
    public void sendSignupMail(AuthCodeRequest request) {
        if(userRepository.existsUserByEmail(request.getEmail())) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "이미 해당 메일을 사용하는 유저가 존재합니다.");
        }
        authCodeRepository.deleteByEmail(request.getEmail());
        AuthCode authCode = authCodeRepository.save(new AuthCode(request, VerifyCodeType.SIGNUP));
        log.info("메일 전송");
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(authCode.getEmail());
        mailMessage.setSubject("washer 이메일 확인 코드 입니다.");
        mailMessage.setText("이메일 인증 코드 입니다.\n" + authCode.getCode());
        javaMailSender.send(mailMessage);
    }
    @Transactional
    public void emailVerify(EmailVerifyRequest request) {
        AuthCode code = authCodeRepository.findByEmail(request.getEmail());
        if (code.isAuthCodeExpired()) {
            authCodeRepository.deleteByEmail(request.getEmail());
            throw new HttpException(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다.");
        }

        if (!code.getCode().equals(request.getCode())) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "잘못된 인증 코드입니다.");
        }
        User user = User.builder()
                .email(request.getEmail())
                .emailVerifyStatus(true)
                .build();
        userRepository.save(user);
        authCodeRepository.deleteByEmail(request.getEmail());
    }

    @Transactional
    public void signup(SignupRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "없는 유저 입니다."));

        if (user.getPassword() != null || user.getName() != null) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "이미 회원가입을 완료한 유저입니다.");
        }

        if(!user.isEmailVerifyStatus()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "인증되지 않은 유저입니다.");
        }

        Room room = roomRepository.findByName(request.getRoom())
                .orElseThrow(() -> new HttpException(HttpStatus.BAD_REQUEST, "존재하지 않는 방입니다."));

        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setSchoolNumber(request.getSchoolNumber());
        user.setRoles(List.of(Role.ROLE_USER));
        user.setGender(Gender.valueOf(request.getGender()));
        user.setRoom(room);
        userRepository.save(user);
    }
}
