package com.example.spakclogin.registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartRegistrationRequest {

    @NotBlank
    @Email
    private String email;
}