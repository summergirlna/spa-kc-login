package com.example.spakclogin.registration.model;

import java.time.OffsetDateTime;

public record AppUser(
        Long id,
        String userId,
        String email,
        String keycloakUserId,
        String mfaType,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
