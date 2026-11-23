package com.example.spakclogin.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfirmEmailChangeRequest {

    @NotBlank
    private String changeId;

    @NotBlank
    @Size(min = 6, max = 6)
    private String code;
}