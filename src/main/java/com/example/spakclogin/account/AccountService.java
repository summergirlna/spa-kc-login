package com.example.spakclogin.account;

import com.example.spakclogin.account.dto.ConfirmEmailChangeResponse;
import com.example.spakclogin.account.dto.StartEmailChangeResponse;
import com.example.spakclogin.account.model.EmailChangeAttempt;
import com.example.spakclogin.account.repository.EmailChangeAttemptRepository;
import com.example.spakclogin.common.code.VerificationCodeIssue;
import com.example.spakclogin.common.code.VerificationCodeIssuer;
import com.example.spakclogin.common.code.VerificationCodeStatus;
import com.example.spakclogin.common.code.VerificationCodeValidator;
import com.example.spakclogin.keycloak.KeycloakAdminService;
import com.example.spakclogin.mail.MailSender;
import com.example.spakclogin.registration.RegistrationException;
import com.example.spakclogin.registration.model.AppUser;
import com.example.spakclogin.registration.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AppUserRepository appUserRepository;
    private final EmailChangeAttemptRepository emailChangeAttemptRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final MailSender mailSender;
    private final VerificationCodeIssuer verificationCodeIssuer;
    private final VerificationCodeValidator verificationCodeValidator;

    @Transactional
    public StartEmailChangeResponse startEmailChange(String keycloakUserId, String newEmail) {
        AppUser appUser = appUserRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new RegistrationException("User was not found."));

        if (appUser.email().equalsIgnoreCase(newEmail)) {
            throw new RegistrationException("New email is same as current email.");
        }

        if (appUserRepository.countByEmail(newEmail) > 0) {
            throw new RegistrationException("Email is already registered.");
        }

        String changeId = UUID.randomUUID().toString();
        VerificationCodeIssue issue = verificationCodeIssuer.issue();

        emailChangeAttemptRepository.insert(
                changeId,
                appUser.userId(),
                appUser.keycloakUserId(),
                appUser.email(),
                newEmail,
                issue.code(),
                VerificationCodeStatus.CODE_SENT.name(),
                issue.expiresAt(),
                issue.issuedAt(),
                issue.issuedAt()
        );

        mailSender.sendEmailChangeCode(
                changeId,
                newEmail,
                issue.code(),
                issue.expiresInMinutes()
        );

        return new StartEmailChangeResponse(
                changeId,
                "Email change code generated. Please check runtime-data/mails."
        );
    }

    @Transactional
    public ConfirmEmailChangeResponse confirmEmailChange(String keycloakUserId, String changeId, String code) {
        EmailChangeAttempt attempt = emailChangeAttemptRepository.findByChangeId(changeId)
                .orElseThrow(() -> new RegistrationException("Email change attempt was not found."));

        if (!attempt.keycloakUserId().equals(keycloakUserId)) {
            throw new RegistrationException("Email change attempt was not found.");
        }

        if (VerificationCodeStatus.COMPLETED.name().equals(attempt.status())) {
            return new ConfirmEmailChangeResponse("Email has already been changed.");
        }

        verificationCodeValidator.validateNotExpired(
                attempt.expiresAt(),
                "Email change code has expired"
        );

        verificationCodeValidator.validateCode(
                attempt.confirmationCode(),
                code,
                "Email change code is invalid"
        );

        OffsetDateTime now = OffsetDateTime.now();

        emailChangeAttemptRepository.updateVerified(
                changeId,
                VerificationCodeStatus.VERIFIED.name(),
                now,
                now
        );

        keycloakAdminService.updateEmail(
                attempt.keycloakUserId(),
                attempt.newEmail()
        );

        appUserRepository.updateEmail(
                attempt.userId(),
                attempt.newEmail(),
                now
        );

        emailChangeAttemptRepository.updateStatus(
                changeId,
                VerificationCodeStatus.COMPLETED.name(),
                now
        );

        return new ConfirmEmailChangeResponse("Email has been changed. Please login again.");
    }
}
