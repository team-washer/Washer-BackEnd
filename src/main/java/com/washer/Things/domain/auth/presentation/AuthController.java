package com.washer.Things.domain.auth.presentation;


import com.washer.Things.domain.auth.presentation.dto.request.*;
import com.washer.Things.domain.auth.presentation.dto.response.ReissueTokenResponse;
import com.washer.Things.domain.auth.presentation.dto.response.SignInResponse;
import com.washer.Things.domain.auth.service.*;
import com.washer.Things.global.exception.dto.response.BaseResponse;
import com.washer.Things.global.security.jwt.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final LogoutService logoutService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원가입")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "이미 회원가입한 유저 / 인증되지 않은 유저 / 존재하지 않는 방"),
            @ApiResponse(responseCode = "404", description = "없는 유저")
    })
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<Void>> signup(@RequestBody @Valid SignupRequest request) {
        signupService.signup(request);
        return ResponseEntity.ok(BaseResponse.success("회원가입 성공"));
    }

    @Operation(summary = "회원가입 인증 메일 발송")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 발송 성공"),
            @ApiResponse(responseCode = "400", description = "이미 존재하는 이메일"),
            @ApiResponse(responseCode = "500", description = "이메일 전송 실패 또는 예기치 못한 오류")
    })
    @PostMapping("/signup/mailsend")
    public ResponseEntity<BaseResponse<Void>> signupMailSend(@RequestBody @Valid AuthCodeRequest request) {
        signupService.sendSignupMail(request);
        return ResponseEntity.ok(BaseResponse.success("회원가입 이메일 발송 성공"));
    }

    @Operation(summary = "회원가입 이메일 인증")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
            @ApiResponse(responseCode = "400", description = "인증 코드 만료 / 잘못된 인증 코드")
    })
    @PostMapping("/signup/emailverify")
    public ResponseEntity<BaseResponse<Void>> signupEmailVerify(@RequestBody @Valid EmailVerifyRequest request) {
        signupService.emailVerify(request);
        return ResponseEntity.ok(BaseResponse.success("이메일 인증 성공"));
    }

    @Operation(summary = "로그인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자"),
            @ApiResponse(responseCode = "401", description = "비밀번호 오류")
    })
    @PostMapping("/signin")
    public ResponseEntity<BaseResponse<SignInResponse>> signin(@RequestBody @Valid SigninRequest request) {
        SignInResponse response = signinService.execute(request);
        return ResponseEntity.ok(BaseResponse.success(response, "로그인 성공"));
    }

    @Operation(summary = "토큰 재발급")
    @Parameter(name = "Refresh-Token", required = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "400", description = "토큰 없음 / 형식 오류 / 잘못된 구성 / 서명 오류 / 알 수 없는 토큰"),
            @ApiResponse(responseCode = "401", description = "만료되거나 유효하지 않은 refreshToken"),
            @ApiResponse(responseCode = "403", description = "만료되거나 잘못된 refreshToken"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    @PostMapping("/reissue")
    public ResponseEntity<BaseResponse<ReissueTokenResponse>> reissueToken(@RequestHeader("Refresh-Token") String refreshHeader) {
        String refreshToken = jwtProvider.resolveToken(refreshHeader);
        ReissueTokenResponse response = refreshService.execute(refreshToken);
        return ResponseEntity.ok(BaseResponse.success(response, "토큰 재발급 성공"));
    }

    @Operation(summary = "비밀번호 변경 메일 발송")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 발송 성공"),
            @ApiResponse(responseCode = "500", description = "이메일 전송 실패 또는 예기치 못한 오류")
    })
    @PostMapping("/pwchange/mailsend")
    public ResponseEntity<BaseResponse<Void>> mailSend(@RequestBody @Valid AuthCodeRequest request) {
        passwordChangeService.sendMail(request);
        return ResponseEntity.ok(BaseResponse.success("비밀번호 변경 메일 발송 성공"));
    }

    @Operation(summary = "비밀번호 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "인증 코드 만료 / 잘못된 인증 코드")
    })
    @PostMapping("/pwchange")
    public ResponseEntity<BaseResponse<Void>> mailCheck(@RequestBody @Valid PasswordChangeRequest request) {
        passwordChangeService.passwordChange(request);
        return ResponseEntity.ok(BaseResponse.success("비밀번호 변경 성공"));
    }

    @Operation(summary = "로그아웃")
    @Parameter(name = "Refresh-Token", required = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "헤더 없음 / 잘못된 값"),
            @ApiResponse(responseCode = "401", description = "토큰 일치하지 않음"),
            @ApiResponse(responseCode = "404", description = "저장된 리프레시 토큰 없음")
    })
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(@RequestHeader("Refresh-Token") String refreshToken) {
        String resolveRefreshToken = jwtProvider.resolveToken(refreshToken);
        logoutService.logout(resolveRefreshToken);
        return ResponseEntity.ok(BaseResponse.success("로그아웃 성공"));
    }
}
