package com.example.spakclogin.registration;

import com.example.spakclogin.keycloak.KeycloakAdminService;
import com.example.spakclogin.mail.MailSender;
import com.example.spakclogin.registration.component.ConfirmationCodeGenerator;
import com.example.spakclogin.registration.component.PasswordValidator;
import com.example.spakclogin.registration.dto.CompleteRegistrationResponse;
import com.example.spakclogin.registration.dto.StartRegistrationResponse;
import com.example.spakclogin.registration.model.RegistrationAttempt;
import com.example.spakclogin.registration.repository.AppUserRepository;
import com.example.spakclogin.registration.repository.RegistrationAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final int CONFIRMATION_CODE_MAX_EXPIRES_MINUTES = 10;

    private static final String REGISTRATION_STATUS_CODE_SENT = "CODE_SENT";
    private static final String REGISTRATION_STATUS_VERIFIED = "VERIFIED";
    private static final String REGISTRATION_STATUS_COMPLETED = "COMPLETED";
    private static final String USER_STATUS_ACTIVE = "ACTIVE";

    private final RegistrationAttemptRepository registrationAttemptRepository;
    private final AppUserRepository appUserRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final MailSender mailSender;
    private final ConfirmationCodeGenerator confirmationCodeGenerator;
    private final PasswordValidator passwordValidator;

    @Transactional
    public StartRegistrationResponse startRegistration(String email) {
        String registrationId = UUID.randomUUID().toString();
        String code = confirmationCodeGenerator.generate();

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiresAt = now.plusMinutes(CONFIRMATION_CODE_MAX_EXPIRES_MINUTES);

        registrationAttemptRepository.insert(
                registrationId,
                email,
                code,
                REGISTRATION_STATUS_CODE_SENT,
                expiresAt,
                now,
                now
        );

        mailSender.sendConfirmationCode(registrationId, email, code, CONFIRMATION_CODE_MAX_EXPIRES_MINUTES);

        return new StartRegistrationResponse(
                registrationId, "Confirmation code generated. Please check runtime-data/mails."
        );
    }

    @Transactional
    public void confirmCode(String registrationId, String code) {
        RegistrationAttempt attempt = findRegistrationAttempt(registrationId);

        if (REGISTRATION_STATUS_VERIFIED.equals(attempt.status())
                || REGISTRATION_STATUS_COMPLETED.equals(attempt.status())) {
            return;
        }

        if (OffsetDateTime.now().isAfter(attempt.expiresAt())) {
            throw new RegistrationException("Confirmation code has expired");
        }

        if (!attempt.confirmationCode().equals(code)) {
            throw new RegistrationException("Confirmation code is invalid");
        }

        OffsetDateTime now = OffsetDateTime.now();

        registrationAttemptRepository.updateVerified(
                registrationId,
                REGISTRATION_STATUS_VERIFIED,
                now,
                now
        );

    }

    @Transactional
    public CompleteRegistrationResponse completeRegistration(String registrationId, String password) {
        RegistrationAttempt attempt = findRegistrationAttempt(registrationId);

        if (!REGISTRATION_STATUS_VERIFIED.equals(attempt.status())) {
            throw new RegistrationException("Confirmation code is not verified");
        }

        passwordValidator.validate(password);

        if (existsUserByEmail(attempt.email())) {
            throw new RegistrationException("Email is already registered");
        }

        String userId = generateNextUserId();
        OffsetDateTime now = OffsetDateTime.now();

        String keycloakUserId = keycloakAdminService.createUser(
                userId,
                attempt.email(),
                password
        );

        appUserRepository.insert(
                userId,
                attempt.email(),
                keycloakUserId,
                USER_STATUS_ACTIVE,
                now,
                now
        );

        registrationAttemptRepository.updateStatus(
                registrationId,
                REGISTRATION_STATUS_COMPLETED,
                now
        );

        return new CompleteRegistrationResponse(
                userId,
                attempt.email(),
                "User registered."
        );
    }

    private boolean existsUserByEmail(String email) {
        return appUserRepository.countByEmail(email) > 0;
    }

    private String generateNextUserId() {
        long nextValue = appUserRepository.nextIdValue();
        return "U%08d".formatted(nextValue);
    }

    private RegistrationAttempt findRegistrationAttempt(String registrationId) {
        return registrationAttemptRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new RegistrationException("Registration attempt was not found."));

    }
}
