package com.example.spakclogin.passwordreset;

import com.example.spakclogin.keycloak.KeycloakAdminService;
import com.example.spakclogin.mail.MailSender;
import com.example.spakclogin.passwordreset.dto.CompletePasswordResetResponse;
import com.example.spakclogin.passwordreset.dto.StartPasswordResetResponse;
import com.example.spakclogin.passwordreset.model.PasswordResetAttempt;
import com.example.spakclogin.passwordreset.repository.PasswordResetAttemptRepository;
import com.example.spakclogin.registration.RegistrationException;
import com.example.spakclogin.registration.component.ConfirmationCodeGenerator;
import com.example.spakclogin.registration.component.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int PASSWORD_RESET_CODE_EXPIRES_MINUTES = 10;

    private static final String PASSWORD_RESET_STATUS_CODE_SENT = "CODE_SENT";
    private static final String PASSWORD_RESET_STATUS_VERIFIED = "VERIFIED";
    private static final String PASSWORD_RESET_STATUS_COMPLETED = "COMPLETED";

    private final PasswordResetAttemptRepository passwordResetAttemptRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final MailSender mailSender;
    private final ConfirmationCodeGenerator confirmationCodeGenerator;
    private final PasswordValidator passwordValidator;

    @Transactional
    public StartPasswordResetResponse startPasswordReset(String email) {
        String keycloakUserId = keycloakAdminService.findUserIdByEmail(email)
                .orElseThrow(() -> new RegistrationException("User was not found."));

        String resetId = UUID.randomUUID().toString();
        String code = confirmationCodeGenerator.generate();

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiresAt = now.plusMinutes(PASSWORD_RESET_CODE_EXPIRES_MINUTES);

        passwordResetAttemptRepository.insert(
                resetId,
                email,
                code,
                PASSWORD_RESET_STATUS_CODE_SENT,
                expiresAt,
                now,
                now
        );

        mailSender.sendPasswordResetCode(
                resetId,
                email,
                code,
                PASSWORD_RESET_CODE_EXPIRES_MINUTES
        );

        return new StartPasswordResetResponse(
                resetId,
                "Password reset code generated. Please check runtime-data/mails."
        );
    }

    @Transactional
    public void confirmCode(String resetId, String code) {
        PasswordResetAttempt attempt = findPasswordResetAttempt(resetId);

        if (PASSWORD_RESET_STATUS_VERIFIED.equals(attempt.status())
                || PASSWORD_RESET_STATUS_COMPLETED.equals(attempt.status())) {
            return;
        }

        if (OffsetDateTime.now().isAfter(attempt.expiresAt())) {
            throw new RegistrationException("Password reset code has expired");
        }

        if (!attempt.confirmationCode().equals(code)) {
            throw new RegistrationException("Password reset code is invalid");
        }

        OffsetDateTime now = OffsetDateTime.now();

        passwordResetAttemptRepository.updateVerified(
                resetId,
                PASSWORD_RESET_STATUS_VERIFIED,
                now,
                now
        );
    }

    @Transactional
    public CompletePasswordResetResponse completePasswordReset(String resetId, String password) {
        PasswordResetAttempt attempt = findPasswordResetAttempt(resetId);

        if (!PASSWORD_RESET_STATUS_VERIFIED.equals(attempt.status())) {
            throw new RegistrationException("Password reset code is not verified");
        }

        passwordValidator.validate(password);

        String keycloakUserId = keycloakAdminService.findUserIdByEmail(attempt.email())
                .orElseThrow(() -> new RegistrationException("User was not found."));

        keycloakAdminService.resetPassword(keycloakUserId, password);

        passwordResetAttemptRepository.updateStatus(
                resetId,
                PASSWORD_RESET_STATUS_COMPLETED,
                OffsetDateTime.now()
        );

        return new CompletePasswordResetResponse("Password has been reset.");
    }

    private PasswordResetAttempt findPasswordResetAttempt(String resetId) {
        return passwordResetAttemptRepository.findByResetId(resetId)
                .orElseThrow(() -> new RegistrationException("Password reset attempt was not found."));
    }
}
