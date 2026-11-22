package com.example.spakclogin.keycloak.emailotp;

import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class EmailOtpAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(EmailOtpAuthenticator.class);

    private static final String MFA_TYPE_ATTRIBUTE = "mfa_type";
    private static final String MFA_TYPE_EMAIL = "EMAIL";
    private static final String EMAIL_OTP_AUTH_NOTE = "email_otp_code";
    private static final String EMAIL_OTP_EXPIRES_AT_AUTH_NOTE = "email_otp_expires_at";
    private static final String EMAIL_OTP_ACTION_RESEND = "resend";
    private static final int EMAIL_OTP_EXPIRES_MINUTES = 5;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();

        if (user == null) {
            logger.debug("Email OTP Authenticator skipped because user is null.");
            context.success();
            return;
        }

        String mfaType = user.getFirstAttribute(MFA_TYPE_ATTRIBUTE);

        if (!MFA_TYPE_EMAIL.equals(mfaType)) {
            logger.infof(
                    "Email OTP Authenticator skipped for user %s because mfa_type is %s.",
                    user.getUsername(),
                    mfaType
            );
            context.success();
            return;
        }

        generateAndStoreOtp(context);

        Response challenge = context.form()
                .createForm("email-otp.ftl");

        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        String action = context.getHttpRequest()
                .getDecodedFormParameters()
                .getFirst("action");

        if (EMAIL_OTP_ACTION_RESEND.equals(action)) {
            generateAndStoreOtp(context);

            Response challenge = context.form()
                    .setInfo("Verification code resent.")
                    .createForm("email-otp.ftl");

            context.challenge(challenge);
            return;
        }

        String otp = context.getHttpRequest()
                .getDecodedFormParameters()
                .getFirst("otp");

        String expectedOtp = context.getAuthenticationSession()
                .getAuthNote(EMAIL_OTP_AUTH_NOTE);

        String expiresAtValue = context.getAuthenticationSession()
                .getAuthNote(EMAIL_OTP_EXPIRES_AT_AUTH_NOTE);

        if (expectedOtp == null || expiresAtValue == null) {
            logger.infof(
                    "Email OTP verification failed because OTP is missing. username=%s",
                    context.getUser().getUsername()
            );

            Response challenge = context.form()
                    .setError("Verification code has expired. Please login again.")
                    .createForm("email-otp.ftl");

            context.challenge(challenge);
            return;
        }

        OffsetDateTime expiresAt = OffsetDateTime.parse(
                expiresAtValue,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
        );

        if (OffsetDateTime.now().isAfter(expiresAt)) {
            logger.infof(
                    "Email OTP verification failed because OTP has expired. username=%s, expiresAt=%s",
                    context.getUser().getUsername(),
                    expiresAt
            );

            clearOtpAuthNotes(context);
            Response challenge = context.form()
                    .setError("Verification code has expired. Please login again.")
                    .createForm("email-otp.ftl");

            context.challenge(challenge);
            return;
        }

        if (expectedOtp.equals(otp)) {
            logger.infof(
                    "Email OTP verification succeeded. username=%s",
                    context.getUser().getUsername()
            );

            clearOtpAuthNotes(context);
            context.success();
            return;
        }

        logger.infof(
                "Email OTP verification failed. username=%s",
                context.getUser().getUsername()
        );

        Response challenge = context.form()
                .setError("Invalid verification code.")
                .createForm("email-otp.ftl");

        context.challenge(challenge);
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }

    private void generateAndStoreOtp(AuthenticationFlowContext context) {
        String otp = generateOtp();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(EMAIL_OTP_EXPIRES_MINUTES);

        context.getAuthenticationSession().setAuthNote(EMAIL_OTP_AUTH_NOTE, otp);
        context.getAuthenticationSession().setAuthNote(
                EMAIL_OTP_EXPIRES_AT_AUTH_NOTE,
                expiresAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );

        logger.infof(
                "Email OTP generated. username=%s, otp=%s, expiresAt=%s",
                context.getUser().getUsername(),
                otp,
                expiresAt
        );
    }

    private String generateOtp() {
        return "%06d".formatted(secureRandom.nextInt(1_000_000));
    }

    private void clearOtpAuthNotes(AuthenticationFlowContext context) {
        context.getAuthenticationSession().removeAuthNote(EMAIL_OTP_AUTH_NOTE);
        context.getAuthenticationSession().removeAuthNote(EMAIL_OTP_EXPIRES_AT_AUTH_NOTE);
    }
}
