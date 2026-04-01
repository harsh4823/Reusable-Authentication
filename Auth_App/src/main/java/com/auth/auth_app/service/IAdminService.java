package com.auth.auth_app.service;

import com.auth.auth_app.model.ClientRegistrationRequest;
import com.auth.auth_app.model.ClientRegistrationResponse;

import java.util.List;

public interface IAdminService {
    ClientRegistrationResponse registerClient(ClientRegistrationRequest clientRegistrationRequest);
    List<ClientRegistrationResponse> getAllClients();
    ClientRegistrationResponse getClient(String clientId);
    ClientRegistrationResponse updateClient(String clientId, ClientRegistrationRequest clientRegistrationRequest);
    void deleteClient(String clientId);
    ClientRegistrationResponse regenerateClientSecret(String clientId);
}
