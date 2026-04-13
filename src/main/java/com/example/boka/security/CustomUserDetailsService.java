package com.example.boka.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final IdentityPort identityPort;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        IdentityPort.IdentityDetails identity = identityPort.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                identity.email(),
                identity.passwordHash() != null ? identity.passwordHash() : "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + identity.role()))
        );
    }
}
