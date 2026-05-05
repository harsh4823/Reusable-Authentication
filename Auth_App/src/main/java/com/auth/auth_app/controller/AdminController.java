package com.auth.auth_app.controller;

import com.auth.auth_app.model.ClientRegistrationRequest;
import com.auth.auth_app.model.ClientRegistrationResponse;
import com.auth.auth_app.service.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final IAdminService adminService;

    @PostMapping("/clients")
    public ResponseEntity<ClientRegistrationResponse> registerClient(@RequestBody ClientRegistrationRequest request){
        ClientRegistrationResponse response = adminService.registerClient(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ClientRegistrationResponse>> getAllClients(){
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getAllClients());
    }

    @GetMapping("/clients/{clientId}")
    public ResponseEntity<ClientRegistrationResponse> getClient(@PathVariable String clientId){
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getClient(clientId));
    }

    @PutMapping("/clients/{clientId}")
    public ResponseEntity<ClientRegistrationResponse> updateClient(@PathVariable String clientId, @RequestBody ClientRegistrationRequest clientRegistrationRequest){
        return ResponseEntity.status(HttpStatus.OK).body(adminService.updateClient(clientId, clientRegistrationRequest));
    }

    @DeleteMapping("/clients/{clientId}")
    public ResponseEntity<String> deleteClient(@PathVariable String clientId) {
        adminService.deleteClient(clientId);
        return ResponseEntity.ok("Client deleted successfully");
    }

    @PostMapping("/clients/{clientId}/regenerate-secret")
    public ResponseEntity<ClientRegistrationResponse> regenerateSecret(@PathVariable String clientId) {
        return ResponseEntity.ok(adminService.regenerateClientSecret(clientId));
    }
}
