package com.auth.auth_app.service.impl;

import com.auth.auth_app.Exception.ResourceNotFoundException;
import com.auth.auth_app.model.ClientRegistrationRequest;
import com.auth.auth_app.model.ClientRegistrationResponse;
import com.auth.auth_app.service.IAdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImp implements IAdminService {

    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public ClientRegistrationResponse registerClient(ClientRegistrationRequest clientRegistrationRequest) {
        String clientId = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();
        String encodedSecret = passwordEncoder.encode(clientSecret);

        RegisteredClient.Builder builder = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(encodedSecret)
                .clientName(clientRegistrationRequest.clientName())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .reuseRefreshTokens(false)
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .build());

        clientRegistrationRequest.redirectUris().forEach(builder::redirectUri);

        clientRegistrationRequest.scopes().forEach(builder::scope);

        clientRegistrationRequest.grantTypes().forEach(grantType -> {
            builder.authorizationGrantType(new AuthorizationGrantType(grantType));
        });

        RegisteredClient client = builder.build();

        registeredClientRepository.save(client);

        return buildResponse(client,clientSecret);
    }

    @Override
    public List<ClientRegistrationResponse> getAllClients() {
        String sql = """
            SELECT client_id, client_name, redirect_uris, scopes,
                   authorization_grant_types, client_id_issued_at
            FROM oauth2_registered_client
           """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            List<String> redirectUris = new ArrayList<>(StringUtils.commaDelimitedListToSet(rs.getString("redirect_uris")));
            List<String> scopes = new ArrayList<>(StringUtils.commaDelimitedListToSet(rs.getString("scopes")));
            List<String> grantTypes = new ArrayList<>(StringUtils.commaDelimitedListToSet(rs.getString("authorization_grant_types")));
            String issuedAt = rs.getTimestamp("client_id_issued_at") != null
                    ? rs.getTimestamp("client_id_issued_at").toInstant().toString()
                    : null;

            return new ClientRegistrationResponse(
                    rs.getString("client_id"),
                    "******", //  Never return the secret hash in a list API
                    rs.getString("client_name"),
                    redirectUris,
                    scopes,
                    grantTypes,
                    issuedAt
            );
        });
    }

    @Override
    public ClientRegistrationResponse getClient(String clientId) {
        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        if (client == null) {
            throw new ResourceNotFoundException("Client", "clientId", clientId);
        }
        return buildResponse(client,"******");

    }

    @Override
    @Transactional
    public ClientRegistrationResponse updateClient(String clientId, ClientRegistrationRequest clientRegistrationRequest) {
        RegisteredClient existingClient = registeredClientRepository.findByClientId(clientId);
        if (existingClient == null) throw new ResourceNotFoundException("Client", "clientId", clientId);

        RegisteredClient.Builder builder = RegisteredClient
                .from(existingClient)
                .redirectUris(uris -> {
                    uris.clear();
                    uris.addAll(clientRegistrationRequest.redirectUris());
                })
                .scopes(scopes -> {
                    scopes.clear();
                    scopes.addAll(clientRegistrationRequest.scopes());
                });

        RegisteredClient updatedClient = builder.build();
        registeredClientRepository.save(updatedClient);
        return buildResponse(updatedClient,"******");
    }

    @Override
    public void deleteClient(String clientId) {
        RegisteredClient  client = registeredClientRepository.findByClientId(clientId);
        if (client == null) throw new ResourceNotFoundException("Client", "clientId", clientId);

        String sql = "DELETE FROM oauth2_registered_client WHERE client_id = ?";

        jdbcTemplate.update(sql,clientId);
    }

    @Override
    public ClientRegistrationResponse regenerateClientSecret(String clientId) {
        RegisteredClient existingClient = registeredClientRepository.findByClientId(clientId);
        if (existingClient == null) throw new ResourceNotFoundException("Client", "clientId", clientId);

        String newClientSecret = UUID.randomUUID().toString();
        String encodedSecret = passwordEncoder.encode(newClientSecret);

        RegisteredClient updatedClient = RegisteredClient.from(existingClient)
                .clientSecret(encodedSecret)
                .build();
        registeredClientRepository.save(updatedClient);
        return buildResponse(updatedClient,newClientSecret);
    }

    private ClientRegistrationResponse buildResponse(RegisteredClient client, String clientSecret) {
        return new ClientRegistrationResponse(
            client.getClientId(),
            clientSecret,
            client.getClientName(),
            new ArrayList<>(client.getRedirectUris()),
            new ArrayList<>(client.getScopes()),
            client.getAuthorizationGrantTypes().stream()
                .map(AuthorizationGrantType::getValue)
                .toList(),
            client.getClientIdIssuedAt() != null
                ? client.getClientIdIssuedAt().toString()
                : Instant.now().toString()

        );
    }
}
