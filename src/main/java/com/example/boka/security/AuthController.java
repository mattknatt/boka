package com.example.boka.security;

import com.example.boka.user.application.UserRegistrationRequest;
import org.springframework.validation.BindingResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final IdentityPort identityPort;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody UserRegistrationRequest registrationRequest,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            authService.registerUser(registrationRequest, request, response);
            return ResponseEntity.ok(Map.of("message", "User registered and logged in successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Object principal = authentication.getPrincipal();
        Map<String, Object> userInfo = new HashMap<>();

        // Extract role from authorities
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .findFirst()
                .map(auth -> auth.substring(5))
                .orElse(null);

        if (principal instanceof OAuth2User oAuth2User) {
            userInfo.put("name", oAuth2User.getAttribute("name"));
            userInfo.put("email", oAuth2User.getAttribute("email"));
            userInfo.put("picture", oAuth2User.getAttribute("picture"));
            userInfo.put("type", "OAUTH2");
            userInfo.put("role", role);
            return ResponseEntity.ok(userInfo);
        } else if (principal instanceof UserDetails userDetails) {
            Optional<IdentityPort.IdentityDetails> identity = identityPort.findByEmail(userDetails.getUsername());
            if (identity.isPresent()) {
                IdentityPort.IdentityDetails user = identity.get();
                userInfo.put("name", user.firstName() + " " + user.lastName());
                userInfo.put("email", user.email());
                userInfo.put("type", "LOCAL");
                userInfo.put("role", role != null ? role : user.role());
                return ResponseEntity.ok(userInfo);
            } else {
                return ResponseEntity.notFound().build();
            }
        }

        return ResponseEntity.status(401).build();
    }
}
