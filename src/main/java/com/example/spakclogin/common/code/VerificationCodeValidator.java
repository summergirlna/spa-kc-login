package com.example.spakclogin.common.code;

import com.example.spakclogin.registration.RegistrationException;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class VerificationCodeValidator {

    public void validateNotExpired(OffsetDateTime expiresAt, String message) {
        if (OffsetDateTime.now().isAfter(expiresAt)) {
            throw new RegistrationException(message);
        }
    }

    public void validateCode(String expectedCode, String actualCode, String message) {
        if (!expectedCode.equals(actualCode)) {
            throw new RegistrationException(message);
        }
    }
}