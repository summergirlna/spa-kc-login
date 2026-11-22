package com.example.spakclogin.passwordreset.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartPasswordResetRequest {

    @NotBlank
    @Email
    private String email;
}
