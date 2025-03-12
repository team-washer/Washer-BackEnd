package com.washer.Things.domain.auth.presentation;


import com.washer.Things.domain.auth.presentation.dto.request.SigninRequest;
import com.washer.Things.domain.auth.presentation.dto.request.SignupRequest;
import com.washer.Things.domain.auth.presentation.dto.response.TokenResponse;
import com.washer.Things.domain.auth.service.RefreshService;
import com.washer.Things.domain.auth.service.SigninService;
import com.washer.Things.domain.auth.service.SignupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final SignupService signupService;
    private final SigninService signinService;
    private final RefreshService refreshService;

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@RequestBody @Valid SignupRequest request) {
        TokenResponse response = signupService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<TokenResponse> signin(@RequestBody @Valid SigninRequest request) {
        TokenResponse response = signinService.signin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(HttpServletRequest request){
        String accessToken = request.getHeader("access");
        String refreshToken = request.getHeader("refresh");
        TokenResponse response = refreshService.refresh(accessToken, refreshToken);
        return ResponseEntity.ok(response);
    }
}
