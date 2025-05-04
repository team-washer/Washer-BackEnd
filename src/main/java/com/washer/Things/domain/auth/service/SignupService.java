package com.washer.Things.domain.auth.service;


import com.washer.Things.domain.auth.presentation.dto.request.AuthCodeRequest;
import com.washer.Things.domain.auth.presentation.dto.request.EmailVerifyRequest;
import com.washer.Things.domain.auth.presentation.dto.request.SignupRequest;

public interface SignupService {
    void signup(SignupRequest request);
    void sendSignupMail(AuthCodeRequest request);
    void emailVerify(EmailVerifyRequest request);
}
