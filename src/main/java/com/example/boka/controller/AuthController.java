package com.example.boka.controller;

import com.example.boka.dto.UserRegistrationRequest;
import com.example.boka.entity.User;
import com.example.boka.repository.UserRepository;
import com.example.boka.service.AuthService;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository; // Still needed for profile details lookup in /me

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
        } else if (principal instanceof UserDetails userDetails) {
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (user != null) {
                userInfo.put("name", user.getFirstName() + " " + user.getLastName());
                userInfo.put("email", user.getEmail());
                userInfo.put("type", "LOCAL");
                userInfo.put("role", role != null ? role : user.getRole().name());
            }
        }

        return ResponseEntity.ok(userInfo);
    }
}
