package com.example.spakclogin;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final int CONFIRMATION_CODE_LENGTH = 6;
    private static final int CONFIRMATION_CODE_MAX_EXPIRES_MINUTES = 10;

    private static final String REGISTRATION_STATUS_CODE_SENT = "CODE_SENT";
    private static final String REGISTRATION_STATUS_VERIFIED = "VERIFIED";
    private static final String REGISTRATION_STATUS_COMPLETED = "COMPLETED";
    private static final String USER_STATUS_ACTIVE = "ACTIVE";

    private final JdbcClient jdbcClient;
    private final KeycloakAdminService keycloakAdminService;

    private final SecureRandom secureRandom = new SecureRandom();

    private final Path runtimeDataPath = Path.of("runtime-data");
    private final Path mailsPath = runtimeDataPath.resolve("mails");

    @Transactional
    public RegistrationController.StartRegistrationResponse startRegistration(String email) {
        createDirectories();

        String registrationId = UUID.randomUUID().toString();
        String code = generateConfirmationCode();

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiresAt = now.plusMinutes(CONFIRMATION_CODE_MAX_EXPIRES_MINUTES);

        jdbcClient.sql("""
                        INSERT INTO registration_attempts (
                            registration_id,
                            email,
                            confirmation_code,
                            status,
                            verified_at,
                            expires_at,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            :registrationId,
                            :email,
                            :confirmationCode,
                            :status,
                            NULL,
                            :expiresAt,
                            :createdAt,
                            :updatedAt
                        )
                        """)
                .param("registrationId", registrationId)
                .param("email", email)
                .param("confirmationCode", code)
                .param("status", REGISTRATION_STATUS_CODE_SENT)
                .param("expiresAt", expiresAt)
                .param("createdAt", now)
                .param("updatedAt", now)
                .update();

        writeMailFile(registrationId, email, code);

        return new RegistrationController.StartRegistrationResponse(
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

        jdbcClient.sql("""
                        UPDATE registration_attempts
                        SET status = :status,
                            verified_at = :verifiedAt,
                            updated_at = :updatedAt
                        WHERE registration_id = :registrationId
                        """)
                .param("status", REGISTRATION_STATUS_VERIFIED)
                .param("verifiedAt", now)
                .param("updatedAt", now)
                .param("registrationId", registrationId)
                .update();

    }

    @Transactional
    public RegistrationController.CompleteRegistrationResponse completeRegistration(String registrationId, String password) {
        RegistrationAttempt attempt = findRegistrationAttempt(registrationId);

        if (!REGISTRATION_STATUS_VERIFIED.equals(attempt.status())) {
            throw new RegistrationException("Confirmation code is not verified");
        }

        validatePassword(password);

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

        jdbcClient.sql("""
                        INSERT INTO app_users (
                            user_id,
                            email,
                            keycloak_user_id,
                            status,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            :userId,
                            :email,
                            :keycloakUserId,
                            :status,
                            :createdAt,
                            :updatedAt
                        )
                        """)
                .param("userId", userId)
                .param("email", attempt.email())
                .param("keycloakUserId", keycloakUserId)
                .param("status", USER_STATUS_ACTIVE)
                .param("createdAt", now)
                .param("updatedAt", now)
                .update();

        jdbcClient.sql("""
                        UPDATE registration_attempts
                        SET status = :status,
                            updated_at = :updatedAt
                        WHERE registration_id = :registrationId
                        """)
                .param("status", REGISTRATION_STATUS_COMPLETED)
                .param("updatedAt", now)
                .param("registrationId", registrationId)
                .update();

        return new RegistrationController.CompleteRegistrationResponse(
                userId,
                attempt.email(),
                "User registered."
        );
    }

    private boolean existsUserByEmail(String email) {
        Integer count = jdbcClient.sql("""
                        SELECT COUNT(*)
                        FROM app_users
                        WHERE email = :email
                        """)
                .param("email", email)
                .query(Integer.class)
                .single();

        return count != null && count > 0;
    }

    private void createDirectories() {
        try {
            Files.createDirectories(mailsPath);
        } catch (IOException exception) {
            throw new RegistrationException("Failed to create runtime data directories");
        }
    }

    private String generateConfirmationCode() {
        int upperBound = (int) Math.pow(10, CONFIRMATION_CODE_LENGTH);
        int value = secureRandom.nextInt(upperBound);
        return String.format("%06d", value);
    }

    public record RegistrationAttempt(
            String registrationId,
            String email,
            String confirmationCode,
            String status,
            OffsetDateTime verifiedAt,
            OffsetDateTime expiresAt,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
    }

    private void writeMailFile(String registrationId, String email, String code) {
        String content = """
                To: %s
                Subject: Confirmation code

                Your confirmation code is: %s
                This code expires in %d minutes.
                """.formatted(email, code, CONFIRMATION_CODE_MAX_EXPIRES_MINUTES);

        try {
            Files.writeString(mailsPath.resolve(registrationId + ".txt"), content);
        } catch (IOException exception) {
            throw new RegistrationException("Failed to write mail file.");
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new RegistrationException("Password must be at least 8 characters.");
        }

        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasLetter || !hasDigit) {
            throw new RegistrationException("Password must contain both letters and digits.");
        }
    }

    private String generateNextUserId() {
        Long nextValue = jdbcClient.sql("""
                        SELECT COALESCE(MAX(id), 0) + 1
                        FROM app_users
                        """)
                .query(Long.class)
                .single();

        return "U%08d".formatted(nextValue);
    }

    private RegistrationAttempt findRegistrationAttempt(String registrationId) {
        return jdbcClient.sql("""
                        SELECT
                            registration_id,
                            email,
                            confirmation_code,
                            status,
                            verified_at,
                            expires_at,
                            created_at,
                            updated_at
                        FROM registration_attempts
                        WHERE registration_id = :registrationId
                        """)
                .param("registrationId", registrationId)
                .query((rs, rowNum) -> new RegistrationAttempt(
                        rs.getString("registration_id"),
                        rs.getString("email"),
                        rs.getString("confirmation_code"),
                        rs.getString("status"),
                        rs.getObject("verified_at", OffsetDateTime.class),
                        rs.getObject("expires_at", OffsetDateTime.class),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("updated_at", OffsetDateTime.class)
                ))
                .optional()
                .orElseThrow(() -> new RegistrationException("Registration attempt was not found."));
    }
}
