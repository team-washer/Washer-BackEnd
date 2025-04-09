package com.washer.Things.domain.auth.service;import com.washer.Things.domain.auth.presentation.dto.request.PasswordChangeCodeRequest;
import com.washer.Things.domain.auth.presentation.dto.request.PwChangeRequest;

public interface PasswordChangeService {
    void sendMail(PasswordChangeCodeRequest request);
    void passwordChange(PwChangeRequest request);
}
