package com.auth.auth_app.config;

import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser authUser = authUserRepository.findByEmail(username)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with username: " + username));
        List<SimpleGrantedAuthority> authorities = authUser.getRoles().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                .toList();
        return new User(authUser.getEmail(), authUser.getPassword(),authorities);
    }


}
