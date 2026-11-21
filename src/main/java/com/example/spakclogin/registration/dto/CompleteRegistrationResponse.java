package com.example.spakclogin.registration.dto;

public record CompleteRegistrationResponse(
        String userId,
        String email,
        String message
) {
}