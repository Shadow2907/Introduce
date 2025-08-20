package com.shino.vnpt.authentication.login;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;
    @Value("${keycloak.realm}")
    private String keycloakRealm;
    @Value("${keycloak.resource}")
    private String keycloakResource;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        RestTemplate restTemplate = new RestTemplate();

        String tokenEndpoint = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakAuthServerUrl,keycloakRealm);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", keycloakResource);
        form.add("username", loginRequest.getUsername());
        form.add("password", loginRequest.getPassword());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenEndpoint, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> map = response.getBody();
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setAccessToken((String) map.get("access_token"));
                loginResponse.setRefreshToken((String) map.get("refresh_token"));
                loginResponse.setExpiresIn(((Number) map.get("expires_in")).longValue());
                loginResponse.setTokenType((String) map.get("token_type"));
                return loginResponse;
            } else {
                throw new RuntimeException("Invalid credentials: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error logging in with keycloak: " + e.getMessage(), e);
        }
    }


    @Override
    public void logout(String refreshToken) {
        String logoutUrl = String.format("%s/realms/%s/protocol/openid-connect/logout",
                keycloakAuthServerUrl, keycloakRealm);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", keycloakResource);
        form.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(logoutUrl, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to logout: " + response.getBody());
        }
    }

}
