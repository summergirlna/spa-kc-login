package com.example.spakclogin.passwordreset.model;

import java.time.OffsetDateTime;

public record PasswordResetAttempt(
        String resetId,
        String email,
        String confirmationCode,
        String status,
        OffsetDateTime verifiedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
