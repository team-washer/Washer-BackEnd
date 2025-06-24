package com.washer.Things.domain.auth.service.impl;

import com.washer.Things.domain.auth.entity.AuthCode;
import com.washer.Things.domain.auth.entity.enums.VerifyCodeType;
import com.washer.Things.domain.auth.presentation.dto.request.AuthCodeRequest;
import com.washer.Things.domain.auth.presentation.dto.request.PasswordChangeRequest;
import com.washer.Things.domain.auth.repository.AuthCodeRepository;
import com.washer.Things.domain.auth.service.PasswordChangeService;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.domain.user.repository.UserRepository;
import com.washer.Things.global.exception.HttpException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PasswordChangeServiceImpl implements PasswordChangeService {
    private final AuthCodeRepository authCodeRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public void sendMail(AuthCodeRequest request) {
        authCodeRepository.deleteByEmail(request.getEmail());
        AuthCode passwordChangeCode = authCodeRepository.save(new AuthCode(request, VerifyCodeType.PASSWORD_RESET));

        String htmlContent = String.format("""
        <!DOCTYPE html>
        <html lang="ko">
        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>Washer 비밀번호 재설정</title>
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
                                    <h2 style="margin: 0 0 16px 0; font-size: 24px; color: #333; text-align: center; font-weight: 600;">비밀번호 재설정 요청</h2>
                                    
                                    <p style="margin: 0 0 32px 0; color: #666; line-height: 1.6; text-align: center; font-size: 15px;">
                                        Washer 계정의 비밀번호를 재설정하려면 아래 인증코드를 입력해주세요.<br>
                                        요청하지 않으셨다면 이 메일을 무시하셔도 됩니다.
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
                                        이 메일은 비밀번호 재설정을 요청하신 분에게만 발송됩니다.<br>
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
        """, passwordChangeCode.getCode());

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(passwordChangeCode.getEmail());
            helper.setSubject("Washer | 비밀번호 재설정 코드입니다.");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }


    @Transactional
    public void passwordChange(PasswordChangeRequest request) {
        AuthCode findCode = authCodeRepository.findByEmail(request.getEmail());
        if (findCode.isAuthCodeExpired()) {
            authCodeRepository.deleteByEmail(request.getEmail());
            throw new HttpException(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다.");
        }

        if (!findCode.getCode().equals(request.getCode())) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "잘못된 인증 코드입니다.");
        }
        updatePassword(request.getPassword(), request.getEmail());
        authCodeRepository.deleteByEmail(request.getEmail());
    }

    private void updatePassword(String newPassword, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "해당 이메일의 사용자가 존재하지 않습니다."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
