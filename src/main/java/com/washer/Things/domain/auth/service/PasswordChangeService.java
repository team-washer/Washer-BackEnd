package com.washer.Things.domain.auth.service;

import com.washer.Things.domain.auth.presentation.dto.request.AuthCodeRequest;
import com.washer.Things.domain.auth.presentation.dto.request.PasswordChangeRequest;

public interface PasswordChangeService {
    void sendMail(AuthCodeRequest request); //메일 전송

    void passwordChange(PasswordChangeRequest request); //메일 인증
}
