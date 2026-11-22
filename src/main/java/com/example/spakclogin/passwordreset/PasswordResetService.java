package com.example.spakclogin.passwordreset;

import com.example.spakclogin.common.code.VerificationCodeIssue;
import com.example.spakclogin.common.code.VerificationCodeIssuer;
import com.example.spakclogin.common.code.VerificationCodeStatus;
import com.example.spakclogin.common.code.VerificationCodeValidator;
import com.example.spakclogin.keycloak.KeycloakAdminService;
import com.example.spakclogin.mail.MailSender;
import com.example.spakclogin.passwordreset.dto.CompletePasswordResetResponse;
import com.example.spakclogin.passwordreset.dto.StartPasswordResetResponse;
import com.example.spakclogin.passwordreset.model.PasswordResetAttempt;
import com.example.spakclogin.passwordreset.repository.PasswordResetAttemptRepository;
import com.example.spakclogin.registration.RegistrationException;
import com.example.spakclogin.registration.component.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetAttemptRepository passwordResetAttemptRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final MailSender mailSender;
    private final VerificationCodeIssuer verificationCodeIssuer;
    private final VerificationCodeValidator verificationCodeValidator;
    private final PasswordValidator passwordValidator;

    @Transactional
    public StartPasswordResetResponse startPasswordReset(String email) {
        String keycloakUserId = keycloakAdminService.findUserIdByEmail(email)
                .orElseThrow(() -> new RegistrationException("User was not found."));

        String resetId = UUID.randomUUID().toString();
        VerificationCodeIssue issue = verificationCodeIssuer.issue();

        passwordResetAttemptRepository.insert(
                resetId,
                email,
                issue.code(),
                VerificationCodeStatus.CODE_SENT.name(),
                issue.expiresAt(),
                issue.issuedAt(),
                issue.issuedAt()
        );

        mailSender.sendPasswordResetCode(
                resetId,
                email,
                issue.code(),
                issue.expiresInMinutes()
        );

        return new StartPasswordResetResponse(
                resetId,
                "Password reset code generated. Please check runtime-data/mails."
        );
    }

    @Transactional
    public StartPasswordResetResponse resendPasswordResetCode(String resetId) {
        PasswordResetAttempt attempt = findPasswordResetAttempt(resetId);

        if (VerificationCodeStatus.VERIFIED.name().equals(attempt.status())
                || VerificationCodeStatus.COMPLETED.name().equals(attempt.status())) {
            throw new RegistrationException("Password reset code cannot be resent for this reset request.");
        }

        VerificationCodeIssue issue = verificationCodeIssuer.issue();

        passwordResetAttemptRepository.updateConfirmationCode(
                resetId,
                issue.code(),
                VerificationCodeStatus.CODE_SENT.name(),
                issue.expiresAt(),
                issue.issuedAt()
        );

        mailSender.sendPasswordResetCode(
                resetId,
                attempt.email(),
                issue.code(),
                issue.expiresInMinutes()
        );

        return new StartPasswordResetResponse(
                resetId,
                "Password reset code resent. Please check runtime-data/mails."
        );
    }

    @Transactional
    public void confirmCode(String resetId, String code) {
        PasswordResetAttempt attempt = findPasswordResetAttempt(resetId);

        if (VerificationCodeStatus.VERIFIED.name().equals(attempt.status())
                || VerificationCodeStatus.COMPLETED.name().equals(attempt.status())) {
            return;
        }

        verificationCodeValidator.validateNotExpired(
                attempt.expiresAt(),
                "Password reset code has expired"
        );

        verificationCodeValidator.validateCode(
                attempt.confirmationCode(),
                code,
                "Password reset code is invalid"
        );

        OffsetDateTime now = OffsetDateTime.now();

        passwordResetAttemptRepository.updateVerified(
                resetId,
                VerificationCodeStatus.VERIFIED.name(),
                now,
                now
        );
    }

    @Transactional
    public CompletePasswordResetResponse completePasswordReset(String resetId, String password) {
        PasswordResetAttempt attempt = findPasswordResetAttempt(resetId);

        if (!VerificationCodeStatus.VERIFIED.name().equals(attempt.status())) {
            throw new RegistrationException("Password reset code is not verified");
        }

        passwordValidator.validate(password);

        String keycloakUserId = keycloakAdminService.findUserIdByEmail(attempt.email())
                .orElseThrow(() -> new RegistrationException("User was not found."));

        keycloakAdminService.resetPassword(keycloakUserId, password);

        passwordResetAttemptRepository.updateStatus(
                resetId,
                VerificationCodeStatus.COMPLETED.name(),
                OffsetDateTime.now()
        );

        return new CompletePasswordResetResponse("Password has been reset.");
    }

    private PasswordResetAttempt findPasswordResetAttempt(String resetId) {
        return passwordResetAttemptRepository.findByResetId(resetId)
                .orElseThrow(() -> new RegistrationException("Password reset attempt was not found."));
    }
}
