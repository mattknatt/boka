package com.example.boka.security;

import com.example.boka.user.application.UserRegistrationRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IdentityPort identityPort;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    @Transactional
    public void registerUser(UserRegistrationRequest registrationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (identityPort.existsByEmail(registrationRequest.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        try {
            IdentityPort.IdentityDetails identity = identityPort.createLocalUser(
                    registrationRequest.getEmail(),
                    registrationRequest.getFirstName(),
                    registrationRequest.getLastName(),
                    passwordEncoder.encode(registrationRequest.getPassword()),
                    "MEMBER"
            );

            // Auto-login after registration
            loginUser(identity, request, response);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email already in use");
        }
    }

    public void loginUser(IdentityPort.IdentityDetails identity, HttpServletRequest request, HttpServletResponse response) {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                identity.email(),
                identity.passwordHash() != null ? identity.passwordHash() : "",
                List.of(new SimpleGrantedAuthority("ROLE_" + identity.role()))
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
