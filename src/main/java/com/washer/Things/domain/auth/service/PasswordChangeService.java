package com.washer.Things.domain.auth.service;

import com.washer.Things.domain.auth.presentation.dto.request.AuthCodeRequest;
import com.washer.Things.domain.auth.presentation.dto.request.PasswordChangeRequest;

public interface PasswordChangeService {
    void sendMail(AuthCodeRequest request);

    void passwordChange(PasswordChangeRequest request);
}
