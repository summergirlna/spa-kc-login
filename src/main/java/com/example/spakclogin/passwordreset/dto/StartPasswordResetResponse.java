package com.example.spakclogin.passwordreset.dto;

public record StartPasswordResetResponse(
        String resetId,
        String message
) {
}
