package com.example.spakclogin.registration;

import com.example.spakclogin.common.code.VerificationCodeIssue;
import com.example.spakclogin.common.code.VerificationCodeIssuer;
import com.example.spakclogin.common.code.VerificationCodeStatus;
import com.example.spakclogin.common.code.VerificationCodeValidator;
import com.example.spakclogin.keycloak.KeycloakAdminService;
import com.example.spakclogin.mail.MailSender;
import com.example.spakclogin.registration.component.PasswordValidator;
import com.example.spakclogin.registration.dto.CompleteRegistrationResponse;
import com.example.spakclogin.registration.dto.StartRegistrationResponse;
import com.example.spakclogin.registration.model.MfaType;
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

    private static final String USER_STATUS_ACTIVE = "ACTIVE";

    private final RegistrationAttemptRepository registrationAttemptRepository;
    private final AppUserRepository appUserRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final MailSender mailSender;
    private final VerificationCodeIssuer verificationCodeIssuer;
    private final VerificationCodeValidator verificationCodeValidator;
    private final PasswordValidator passwordValidator;

    /// 仮登録開始処理
    ///
    /// 1. 仮登録IDを生成する
    /// 2. 仮登録用コードを生成する
    /// 3. 仮登録の有効期限を設定する
    /// 4. 仮登録用レコード情報を生成する(ステータスは仮登録コード送信)
    /// 5. 仮登録メールを送信する
    /// @param email メールアドレス
    /// @return @{@link StartRegistrationResponse}
    @Transactional
    public StartRegistrationResponse startRegistration(String email) {
        String registrationId = UUID.randomUUID().toString();
        VerificationCodeIssue issue = verificationCodeIssuer.issue();

        registrationAttemptRepository.insert(
                registrationId,
                email,
                issue.code(),
                VerificationCodeStatus.CODE_SENT.name(),
                issue.expiresAt(),
                issue.issuedAt(),
                issue.issuedAt()
        );

        mailSender.sendConfirmationCode(
                registrationId,
                email,
                issue.code(),
                issue.expiresInMinutes()
        );

        return new StartRegistrationResponse(
                registrationId, "Confirmation code generated. Please check runtime-data/mails."
        );
    }

    @Transactional
    public StartRegistrationResponse resendConfirmationCode(String registrationId) {
        RegistrationAttempt attempt = findRegistrationAttempt(registrationId);

        if (VerificationCodeStatus.VERIFIED.name().equals(attempt.status())
                || VerificationCodeStatus.COMPLETED.name().equals(attempt.status())) {
            throw new RegistrationException("Confirmation code cannot be resent for this registration.");
        }

        VerificationCodeIssue issue = verificationCodeIssuer.issue();

        registrationAttemptRepository.updateConfirmationCode(
                registrationId,
                issue.code(),
                VerificationCodeStatus.CODE_SENT.name(),
                issue.expiresAt(),
                issue.issuedAt()
        );

        mailSender.sendConfirmationCode(
                registrationId,
                attempt.email(),
                issue.code(),
                issue.expiresInMinutes()
        );

        return new StartRegistrationResponse(
                registrationId,
                "Confirmation code resent. Please check runtime-data/mails."
        );
    }

    @Transactional
    public void confirmCode(String registrationId, String code) {
        RegistrationAttempt attempt = findRegistrationAttempt(registrationId);

        if (VerificationCodeStatus.VERIFIED.name().equals(attempt.status())
                || VerificationCodeStatus.COMPLETED.name().equals(attempt.status())) {
            return;
        }

        verificationCodeValidator.validateNotExpired(
                attempt.expiresAt(),
                "Confirmation code has expired");

        verificationCodeValidator.validateCode(
                attempt.confirmationCode(),
                code,
                "Confirmation code is invalid");

        OffsetDateTime now = OffsetDateTime.now();

        registrationAttemptRepository.updateVerified(
                registrationId,
                VerificationCodeStatus.VERIFIED.name(),
                now,
                now
        );

    }

    @Transactional
    public CompleteRegistrationResponse completeRegistration(String registrationId, String password, MfaType mfaType) {
        RegistrationAttempt attempt = findRegistrationAttempt(registrationId);

        if (!VerificationCodeStatus.VERIFIED.name().equals(attempt.status())) {
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
                password,
                mfaType
        );

        appUserRepository.insert(
                userId,
                attempt.email(),
                keycloakUserId,
                mfaType.name(),
                USER_STATUS_ACTIVE,
                now,
                now
        );

        registrationAttemptRepository.updateStatus(
                registrationId,
                VerificationCodeStatus.COMPLETED.name(),
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
