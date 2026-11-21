package com.example.spakclogin.registration.component;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ConfirmationCodeGenerator {

    private static final int CONFIRMATION_CODE_LENGTH = 6;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        int upperBound = (int) Math.pow(10, CONFIRMATION_CODE_LENGTH);
        int value = secureRandom.nextInt(upperBound);
        return "%06d".formatted(value);
    }
}