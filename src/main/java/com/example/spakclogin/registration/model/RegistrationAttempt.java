package com.example.spakclogin.registration.model;

import java.time.OffsetDateTime;

public record RegistrationAttempt(
        String registrationId,
        String email,
        String confirmationCode,
        String status,
        OffsetDateTime verifiedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
