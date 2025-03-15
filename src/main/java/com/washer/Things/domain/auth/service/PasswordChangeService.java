package com.washer.Things.domain.auth.service;

import com.washer.Things.domain.auth.presentation.dto.request.AuthCodeRequest;
import com.washer.Things.domain.auth.presentation.dto.request.PwChangeRequest;

public interface PasswordChangeService {
    void sendMail(AuthCodeRequest request);
    void passwordChange(PwChangeRequest request);
}
