package com.auth.auth_app.config;

import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RealmAwareUserDetailsService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;
    private final RegisteredClientRepository registeredClientRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email;
        String realmName = null;

        if (username.contains("::")) {
            String[] parts = username.split("::", 2);
            String clientId = parts[0];
            email = parts[1];

            RegisteredClient client = registeredClientRepository.findByClientId(clientId);
            if (client != null) {
                realmName = client.getClientSettings().getSetting("realm");
            }
        } else {
            email = username;
        }

        AuthUser authUser = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (realmName != null) {
            final String finalRealmName = realmName;
            boolean hasAccess = (authUser.getMemberRealm() != null
                    && authUser.getMemberRealm().getRealmName().equals(finalRealmName))
                    || authUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

            if (!hasAccess) {
                throw new UsernameNotFoundException(
                    "User " + email + " is not a member of realm: " + realmName);
            }
        }

        return new User(
            authUser.getEmail(),
            authUser.getPassword() != null ? authUser.getPassword() : "{noop}oauth2-user",
            authUser.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getName()))
                .toList()
        );
    }
}