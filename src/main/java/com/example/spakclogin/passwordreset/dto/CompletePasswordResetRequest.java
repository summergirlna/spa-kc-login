package com.example.spakclogin.passwordreset.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompletePasswordResetRequest {

    @NotBlank
    private String resetId;

    @NotBlank
    private String password;
}