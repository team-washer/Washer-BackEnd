package com.washer.Things.domain.auth.presentation;


import com.washer.Things.domain.auth.presentation.dto.request.*;
import com.washer.Things.domain.auth.presentation.dto.response.ReissueTokenResponse;
import com.washer.Things.domain.auth.presentation.dto.response.SignInResponse;
import com.washer.Things.domain.auth.service.*;
import com.washer.Things.global.exception.dto.response.ApiResponse;
import com.washer.Things.global.security.jwt.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final SignupService signupService;
    private final SigninService signinService;
    private final RefreshService refreshService;
    private final PasswordChangeService passwordChangeService;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody @Valid SignupRequest request) {
        signupService.signup(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입 성공"));
    }

    @PostMapping("/signup/mailsend")
    public ResponseEntity<ApiResponse<Void>> signupMailSend(@RequestBody @Valid AuthCodeRequest request) {
        signupService.sendSignupMail(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입 이메일 발송 성공"));
    }

    @PostMapping("/signup/emailverify")
    public ResponseEntity<ApiResponse<Void>> signupEmailVerify(@RequestBody @Valid EmailVerifyRequest request) {
        signupService.emailVerify(request);
        return ResponseEntity.ok(ApiResponse.success("이메일 인증 성공"));
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<SignInResponse>> signin(@RequestBody @Valid SigninRequest request) {
        SignInResponse response = signinService.execute(request);
        return ResponseEntity.ok(ApiResponse.success(response, "로그인 성공"));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueTokenResponse>> reissueToken(@RequestHeader("Refresh-Token") String refreshHeader) {
        String refreshToken = jwtProvider.resolveToken(refreshHeader);
        ReissueTokenResponse response = refreshService.execute(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response, "토큰 재발급 성공"));
    }

    @PostMapping("/pwchange/mailsend")
    public ResponseEntity<ApiResponse<Void>> mailSend(@RequestBody @Valid AuthCodeRequest request) {
        passwordChangeService.sendMail(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 메일 발송 성공"));
    }

    @PostMapping("/pwchange")
    public ResponseEntity<ApiResponse<Void>> mailCheck(@RequestBody @Valid PasswordChangeRequest request) {
        passwordChangeService.passwordChange(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 성공"));
    }
}
