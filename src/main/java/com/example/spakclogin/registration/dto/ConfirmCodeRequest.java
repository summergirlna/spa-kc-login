package com.example.spakclogin.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfirmCodeRequest {

    @NotBlank
    private String registrationId;

    @NotBlank
    @Size(min = 6, max = 6)
    private String code;
}