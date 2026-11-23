package com.example.spakclogin.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartEmailChangeRequest {

    @NotBlank
    @Email
    private String newEmail;
}