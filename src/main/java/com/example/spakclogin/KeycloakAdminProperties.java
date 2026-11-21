package com.example.spakclogin;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.keycloak.admin")
public class KeycloakAdminProperties {
    private String serverUrl;

    private String realm;

    private String adminRealm;

    private String clientId;

    private String username;

    private String password;
}
