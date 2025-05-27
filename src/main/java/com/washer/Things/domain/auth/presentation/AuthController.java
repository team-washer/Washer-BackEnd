package com.washer.Things.domain.auth.presentation;


import com.washer.Things.domain.auth.presentation.dto.request.*;
import com.washer.Things.domain.auth.presentation.dto.response.ReissueTokenResponse;
import com.washer.Things.domain.auth.presentation.dto.response.SignInResponse;
import com.washer.Things.domain.auth.service.*;
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
    private final GetUser getUser;

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
        signupService.emailVerify(request);
    }

    @PostMapping("/signin")
    public ResponseEntity<SignInResponse> signin(@RequestBody @Valid SigninRequest request) {
        SignInResponse response = signinService.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reissue")
    public ReissueTokenResponse reissueToken(@RequestHeader("Refresh-Token") String refreshHeader) {
        String refreshToken = jwtProvider.resolveToken(refreshHeader);
        return refreshService.execute(refreshToken);
    }

    @PostMapping( "/pwchange/mailsend")
    public void mailSend(@RequestBody @Valid AuthCodeRequest request){
        passwordChangeService.sendMail(request);
    }

    @PostMapping("/pwchange")
    public void mailCheck(@RequestBody @Valid PasswordChangeRequest request) {
        passwordChangeService.passwordChange(request);
    }


}
