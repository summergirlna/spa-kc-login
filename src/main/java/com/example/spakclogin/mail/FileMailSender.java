package com.example.spakclogin.mail;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FileMailSender implements MailSender {

    private final Path runtimeDataPath = Path.of("runtime-data");
    private final Path mailsPath = runtimeDataPath.resolve("mails");

    @Override
    public void sendConfirmationCode(String registrationId, String email, String code, int expiresInMinutes) {
        createDirectories();

        String content = """
                To: %s
                Subject: Confirmation code

                Your confirmation code is: %s
                This code expires in %d minutes.
                """.formatted(email, code, expiresInMinutes);

        try {
            Files.writeString(mailsPath.resolve(registrationId + ".txt"), content);
        } catch (IOException exception) {
            throw new MailSendException("Failed to write mail file.", exception);
        }
    }

    @Override
    public void sendPasswordResetCode(String resetId, String email, String code, int expiresInMinutes) {
        createDirectories();

        String content = """
                To: %s
                Subject: Password reset code

                Your password reset code is: %s
                This code expires in %d minutes.
                """.formatted(email, code, expiresInMinutes);

        try {
            Files.writeString(mailsPath.resolve("password-reset-" + resetId + ".txt"), content);
        } catch (IOException exception) {
            throw new MailSendException("Failed to write mail file.", exception);
        }
    }

    @Override
    public void sendEmailChangeCode(String changeId, String email, String code, int expiresInMinutes) {
        createDirectories();

        String content = """
                To: %s
                Subject: Email change code

                Your email change code is: %s
                This code expires in %d minutes.
                """.formatted(email, code, expiresInMinutes);

        try {
            Files.writeString(mailsPath.resolve("email-change-" + changeId + ".txt"), content);
        } catch (IOException exception) {
            throw new MailSendException("Failed to write mail file.", exception);
        }
    }

    private void createDirectories() {
        try {
            Files.createDirectories(mailsPath);
        } catch (IOException exception) {
            throw new MailSendException("Failed to create mail directories.", exception);
        }
    }
}
