package com.example.spakclogin.registration.component;

import com.example.spakclogin.registration.RegistrationException;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {

    public void validate(String password) {
        if (password.length() < 8) {
            throw new RegistrationException("Password must be at least 8 characters.");
        }

        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasLetter || !hasDigit) {
            throw new RegistrationException("Password must contain both letters and digits.");
        }
    }
}