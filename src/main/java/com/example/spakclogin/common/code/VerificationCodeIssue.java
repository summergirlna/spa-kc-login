package com.example.spakclogin.common.code;

import java.time.OffsetDateTime;

public record VerificationCodeIssue(
        String code,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt,
        int expiresInMinutes
) {
}
