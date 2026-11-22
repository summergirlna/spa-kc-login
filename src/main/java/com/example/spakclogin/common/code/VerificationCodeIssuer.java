package com.example.spakclogin.common.code;

import com.example.spakclogin.registration.component.ConfirmationCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class VerificationCodeIssuer {

    private static final int DEFAULT_EXPIRES_MINUTES = 10;

    private final ConfirmationCodeGenerator confirmationCodeGenerator;

    public VerificationCodeIssue issue() {
        OffsetDateTime now = OffsetDateTime.now();

        return new VerificationCodeIssue(
                confirmationCodeGenerator.generate(),
                now,
                now.plusMinutes(DEFAULT_EXPIRES_MINUTES),
                DEFAULT_EXPIRES_MINUTES
        );
    }
}