package com.example.spakclogin.passwordreset.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendPasswordResetCodeRequest {

    @NotBlank
    private String resetId;
}