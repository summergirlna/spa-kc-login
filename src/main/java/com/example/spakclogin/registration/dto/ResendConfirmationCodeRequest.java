package com.example.spakclogin.registration.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendConfirmationCodeRequest {

    @NotBlank
    private String registrationId;
}
