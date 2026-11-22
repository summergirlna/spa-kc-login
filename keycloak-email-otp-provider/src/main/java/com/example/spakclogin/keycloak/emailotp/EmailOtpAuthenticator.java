package com.example.spakclogin.keycloak.emailotp;

import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.security.SecureRandom;

public class EmailOtpAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(EmailOtpAuthenticator.class);

    private static final String MFA_TYPE_ATTRIBUTE = "mfa_type";
    private static final String MFA_TYPE_EMAIL = "EMAIL";
    private static final String EMAIL_OTP_AUTH_NOTE = "email_otp_code";
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

        String otp = generateOtp();
        context.getAuthenticationSession().setAuthNote(EMAIL_OTP_AUTH_NOTE, otp);

        logger.infof(
                "Email OTP challenge displayed. username=%s, mfa_type=%s, otp=%s",
                user.getUsername(),
                mfaType,
                otp
        );

        Response challenge = context.form()
                .createForm("email-otp.ftl");

        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        String otp = context.getHttpRequest()
                .getDecodedFormParameters()
                .getFirst("otp");

        String expectedOtp = context.getAuthenticationSession()
                .getAuthNote(EMAIL_OTP_AUTH_NOTE);

        if (expectedOtp != null && expectedOtp.equals(otp)) {
            logger.infof(
                    "Email OTP verification succeeded. username=%s",
                    context.getUser().getUsername()
            );
            context.getAuthenticationSession().removeAuthNote(EMAIL_OTP_AUTH_NOTE);
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

    private String generateOtp() {
        return "%06d".formatted(secureRandom.nextInt(1_000_000));
    }
}
