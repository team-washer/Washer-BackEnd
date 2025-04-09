package com.washer.Things.domain.auth.presentation;


import com.washer.Things.domain.auth.presentation.dto.request.*;
import com.washer.Things.domain.auth.presentation.dto.response.TokenResponse;
import com.washer.Things.domain.auth.service.PasswordChangeService;
import com.washer.Things.domain.auth.service.RefreshService;
import com.washer.Things.domain.auth.service.SigninService;
import com.washer.Things.domain.auth.service.SignupService;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("/signup")
    public void signup(@RequestBody @Valid SignupRequest request) {
        signupService.signup(request);
    }

    @PostMapping("/signup/mailsend")
    public void signupMailSend(@RequestBody @Valid AuthCodeRequest request){
        signupService.sendSignupMail(request);
    }

    @PostMapping("/signup/emailverify")
    public void signupEmailVerify(@RequestBody @Valid EmailVerifyRequest request){
        signupService.
    }

    @PostMapping("/signin")
    public ResponseEntity<TokenResponse> signin(@RequestBody @Valid SigninRequest request) {
        TokenResponse response = signinService.signin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestHeader("Refresh-Token") String refreshToken){
        TokenResponse response = refreshService.refresh(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping( "/mailsend")
    public void mailSend(@RequestBody @Valid PasswordChangeCodeRequest request){
        passwordChangeService.sendMail(request);
    }

    @PostMapping("/pwchange")
    public void mailCheck(@RequestBody @Valid PwChangeRequest request) {
        passwordChangeService.passwordChange(request);
    }
}
