package com.example.spakclogin.account.model;

import java.time.OffsetDateTime;

public record EmailChangeAttempt(
        String changeId,
        String userId,
        String keycloakUserId,
        String currentEmail,
        String newEmail,
        String confirmationCode,
        String status,
        OffsetDateTime verifiedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
