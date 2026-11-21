package com.example.spakclogin.keycloak;

import com.example.spakclogin.registration.RegistrationException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakAdminService {

    private final KeycloakAdminProperties properties;

    private final RestClient restClient = RestClient.create();

    public String createUser(String userId, String email, String password) {
        String accessToken = getAdminAccessToken();

        try {
            return restClient.post()
                    .uri(properties.getServerUrl() + "/admin/realms/" + properties.getRealm() + "/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CreateUserRequest(
                            userId,
                            email,
                            true,
                            true,
                            List.of(new CredentialRequest(
                                    "password",
                                    password,
                                    false
                            ))
                    ))
                    .exchange((request, response) -> {
                        if (response.getStatusCode().isError()) {
                            throw new RegistrationException("Failed to create Keycloak user.");
                        }

                        URI location = response.getHeaders().getLocation();
                        if (location == null) {
                            return "";
                        }

                        String path = location.getPath();
                        return path.substring(path.lastIndexOf('/') + 1);
                    });
        } catch (HttpClientErrorException.Conflict exception) {
            throw new RegistrationException("Keycloak user already exists.");
        }
    }

    private String getAdminAccessToken() {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", properties.getClientId());
        form.add("username", properties.getUsername());
        form.add("password", properties.getPassword());

        TokenResponse response = restClient.post()
                .uri(properties.getServerUrl()
                        + "/realms/"
                        + properties.getAdminRealm()
                        + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new RegistrationException("Failed to get Keycloak admin access token.");
        }

        return response.accessToken();
    }

    public record TokenResponse(
            @JsonProperty("access_token")
            String accessToken
    ) {
    }

    public record CreateUserRequest(
            String username,
            String email,
            Boolean enabled,
            Boolean emailVerified,
            List<CredentialRequest> credentials
    ) {
    }

    public record CredentialRequest(
            String type,
            String value,
            Boolean temporary
    ) {
    }
}
