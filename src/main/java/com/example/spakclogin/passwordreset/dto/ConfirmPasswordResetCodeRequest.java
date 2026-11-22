package com.example.spakclogin.passwordreset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfirmPasswordResetCodeRequest {

    @NotBlank
    private String resetId;

    @NotBlank
    @Size(min = 6, max = 6)
    private String code;
}