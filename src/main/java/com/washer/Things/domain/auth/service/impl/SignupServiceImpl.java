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
import com.washer.Things.global.auditLog.Auditable;
import com.washer.Things.global.exception.HttpException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
        if (userRepository.existsUserByEmail(request.getEmail())) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "이미 해당 메일을 사용하는 유저가 존재합니다.");
        }

        authCodeRepository.deleteByEmail(request.getEmail());
        AuthCode authCode = authCodeRepository.save(new AuthCode(request, VerifyCodeType.SIGNUP));

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(authCode.getEmail());
            helper.setSubject("Washer | 회원가입 인증 코드입니다.");

            String html = String.format("""
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Washer 회원가입 인증</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5;">
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 0; padding: 20px;">
                    <tr>
                        <td align="center">
                            <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="max-width: 600px; width: 100%%; background-color: white; border-radius: 12px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); overflow: hidden;">
                                <tr>
                                    <td style="background-color: #2C7EF8; padding: 40px 30px; text-align: center;">
                                        <h1 style="margin: 0 0 8px 0; font-size: 36px; font-weight: 700; color: white; letter-spacing: -0.5px;">Washer</h1>
                                        <p style="margin: 0; color: rgba(255, 255, 255, 0.9); font-size: 16px; font-weight: 400;">GSM 세탁/건조기 예약 시스템</p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="margin: 0 0 16px 0; font-size: 24px; color: #333; text-align: center; font-weight: 600;">환영합니다!</h2>
                                        
                                        <p style="margin: 0 0 32px 0; color: #666; line-height: 1.6; text-align: center; font-size: 15px;">
                                            Washer 회원가입을 완료하기 위해 아래 인증코드를 입력해주세요.<br>
                                            편리한 세탁/건조기 예약 서비스를 이용하실 수 있습니다.
                                        </p>

                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 32px 0;">
                                            <tr>
                                                <td style="background-color: #f8f9ff; border: 2px solid #2C7EF8; border-radius: 12px; padding: 32px; text-align: center;">
                                                    <p style="margin: 0 0 16px 0; font-size: 14px; color: #2C7EF8; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">인증 코드</p>
                                                    <p style="margin: 0; font-size: 32px; font-weight: 700; color: #2C7EF8; letter-spacing: 6px; font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;">%s</p>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 0;">
                                            <tr>
                                                <td style="background-color: #fff8e1; border: 1px solid #ffcc02; border-radius: 8px; padding: 16px; text-align: center;">
                                                    <p style="margin: 0; color: #b8860b; font-size: 14px;">⚠️ 이 인증코드는 3분간 유효합니다. 본인이 요청하지 않았다면 이 메일을 무시해주세요.</p>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 24px 30px; text-align: center; border-top: 1px solid #e9ecef;">
                                        <p style="margin: 0 0 8px 0; color: #6c757d; font-size: 13px; line-height: 1.5;">
                                            이 메일은 회원가입을 요청하신 분에게만 발송됩니다.<br>
                                            문의사항이 있으시면 <a href="mailto:teamwasher1@gmail.com" style="color: #2C7EF8; text-decoration: none;">이 메일로 문의해주세요</a>.
                                        </p>
                                        <p style="margin: 0; color: #2C7EF8; font-weight: 600; font-size: 14px;">Washer 팀</p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """, authCode.getCode());

            helper.setText(html, true);
            javaMailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
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
    @Auditable(action = "CREATE", resourceType = "User")
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
