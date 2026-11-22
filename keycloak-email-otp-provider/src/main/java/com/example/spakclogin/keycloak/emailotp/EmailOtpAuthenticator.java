package com.example.spakclogin.keycloak.emailotp;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class EmailOtpAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(EmailOtpAuthenticator.class);

    private static final String MFA_TYPE_ATTRIBUTE = "mfa_type";
    private static final String MFA_TYPE_EMAIL = "EMAIL";

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

        logger.infof(
                "Email OTP Authenticator target user detected. username=%s, mfa_type=%s",
                user.getUsername(),
                mfaType
        );

        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        context.success();
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
}
