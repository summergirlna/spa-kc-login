package com.example.spakclogin.registration.dto;

import com.example.spakclogin.registration.model.MfaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompleteRegistrationRequest {

    @NotBlank
    private String registrationId;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotNull
    private MfaType mfaType;
}