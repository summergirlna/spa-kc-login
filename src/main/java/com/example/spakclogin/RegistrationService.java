package com.example.spakclogin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    private final ObjectMapper objectMapper;

    private final SecureRandom secureRandom = new SecureRandom();

    private final Path runtimeDataPath = Path.of("runtime-data");
    private final Path registrationAttemptsPath = runtimeDataPath.resolve("registration-attempts");
    private final Path mailsPath = runtimeDataPath.resolve("mails");
    private final Path usersPath = runtimeDataPath.resolve("users");
    private final KeycloakAdminService keycloakAdminService;

    public RegistrationController.StartRegistrationResponse startRegistration(String email) {
        createDirectories();

        String registrationId = UUID.randomUUID().toString();
        String code = generateConfirmationCode();

        OffsetDateTime now = OffsetDateTime.now();
        RegistrationAttempt attempt = new RegistrationAttempt(
                registrationId,
                email,
                code,
                false,
                now,
                now.plusMinutes(CONFIRMATION_CODE_MAX_EXPIRES_MINUTES)
        );

        writeJson(registrationAttemptsPath.resolve(registrationId + ".json"), attempt);
        writeMailFile(registrationId, email, code);

        return new RegistrationController.StartRegistrationResponse(
                registrationId, "Confirmation code generated. Please check runtime-data/mails."
        );
    }

    public void confirmCode(String registrationId, String code) {
        RegistrationAttempt attempt = readRegistrationAttempt(registrationId);

        if (attempt.verified()) {
            return;
        }

        if (OffsetDateTime.now().isAfter(attempt.expiresAt())) {
            throw new RegistrationException("Confirmation code has expired");
        }

        if (!attempt.code().equals(code)) {
            throw new RegistrationException("Confirmation code is invalid");
        }

        RegistrationAttempt verifiedAttempt = new RegistrationAttempt(
                attempt.registrationId(),
                attempt.email(),
                attempt.code(),
                true,
                attempt.createdAt(),
                attempt.expiresAt()
        );

        writeJson(registrationAttemptsPath.resolve(registrationId + ".json"), verifiedAttempt);
    }

    public RegistrationController.CompleteRegistrationResponse completeRegistration(String registrationId, String password) {
        createDirectories();

        RegistrationAttempt attempt = readRegistrationAttempt(registrationId);

        if (!attempt.verified()) {
            throw new RegistrationException("Confirmation code is not verified");
        }

        validatePassword(password);

        String userId = generateNextUserId();
        OffsetDateTime now = OffsetDateTime.now();

        String keycloakUserId = keycloakAdminService.createUser(
                userId,
                attempt.email(),
                password
        );

        RegisteredUser registeredUser = new RegisteredUser(
                userId,
                attempt.email(),
                keycloakUserId,
                now,
                "Password was validated but not stored in this training step."
        );

        writeJson(usersPath.resolve(userId + ".json"), registeredUser);

        return new RegistrationController.CompleteRegistrationResponse(
                userId,
                attempt.email(),
                "User registered."
        );
    }

    private void createDirectories() {
        try {
            Files.createDirectories(registrationAttemptsPath);
            Files.createDirectories(mailsPath);
            Files.createDirectories(usersPath);
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
            String code,
            boolean verified,
            OffsetDateTime createdAt,
            OffsetDateTime expiresAt
    ) {
    }

    public record RegisteredUser(
            String userId,
            String email,
            String keycloakUserId,
            OffsetDateTime createdAt,
            String note
    ) {
    }

    private void writeJson(Path path, Object value) {
        objectMapper.writeValue(path.toFile(), value);
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

    private RegistrationAttempt readRegistrationAttempt(String registrationId) {
        Path path = registrationAttemptsPath.resolve(registrationId + ".json");

        if (!Files.exists(path)) {
            throw new RegistrationException("Registration attempt was not found.");
        }

        return objectMapper.readValue(path.toFile(), RegistrationAttempt.class);
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
        long nextNumber;

        try (Stream<Path> files = Files.exists(usersPath) ? Files.list(usersPath) : Stream.empty()) {
            long count = files
                    .filter(path -> path.getFileName().toString().startsWith("U"))
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .count();

            nextNumber = count + 1;
        } catch (IOException exception) {
            throw new RegistrationException("Failed to generate user ID.");
        }

        return "U%08d".formatted(nextNumber);
    }
}
