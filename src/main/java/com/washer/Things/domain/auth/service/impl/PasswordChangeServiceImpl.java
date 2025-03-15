package com.washer.Things.domain.auth.service.impl;

import com.washer.Things.domain.auth.entity.AuthCode;
import com.washer.Things.domain.auth.presentation.dto.request.AuthCodeRequest;
import com.washer.Things.domain.auth.presentation.dto.request.PwChangeRequest;
import com.washer.Things.domain.auth.repository.AuthCodeRepository;
import com.washer.Things.domain.auth.service.PasswordChangeService;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordChangeServiceImpl implements PasswordChangeService {

    private final AuthCodeRepository authCodeRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    @Transactional
    public void sendMail(AuthCodeRequest request) {
        if (authCodeRepository.existsByEmail(request.getEmail())) {
            authCodeRepository.deleteByEmail(request.getEmail());
        }
        AuthCode authCode = authCodeRepository.save(new AuthCode(request));

        if (authCode.getCode() != null) {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(authCode.getEmail());
            mailMessage.setSubject("washer 비밀번호 변경 확인 코드 입니다.");
            mailMessage.setText("비밀번호 변경 인증 코드 입니다.\n" + authCode.getCode());
            javaMailSender.send(mailMessage);
        }
    }

    @Transactional
    public void passwordChange(PwChangeRequest request) {
        AuthCode findCode = authCodeRepository.findByEmail(request.getEmail());
        UUID requestCode = UUID.fromString(request.getCode());
        if (findCode == null) {
            throw new RuntimeException("인증 코드가 존재하지 않습니다.");
        }
        if (request.getPassword() == null) {
            throw new RuntimeException("새 비밀번호가 존재하지 않습니다.");
        }
        if (requestCode.equals(findCode.getCode())) {
            updatePassword(request.getPassword(), request.getEmail());
            authCodeRepository.deleteByEmail(request.getEmail());
        } else {
            throw new RuntimeException("잘못된 인증 코드입니다.");
        }
    }

    private void updatePassword(String newPassword, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("해당 이메일의 사용자가 존재하지 않습니다."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
