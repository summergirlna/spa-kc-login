package com.example.spakclogin.mail;

public interface MailSender {
    void sendConfirmationCode(String registrationId, String email, String code, int expiresInMinutes);
}
