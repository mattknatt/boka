package com.example.boka.gymclass.infrastructure;

import com.example.boka.gymclass.application.GymClassResponse;
import com.example.boka.gymclass.application.ClassSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassSearchController {

    private final ClassSearchService classSearchService;

    @GetMapping("/search")
    public ResponseEntity<Page<GymClassResponse>> searchClasses(
            @RequestParam String query,
            @PageableDefault(size = 6) Pageable pageable,
            Authentication authentication
    ) {
        String email = getEmailFromAuthentication(authentication);
        Page<GymClassResponse> results = classSearchService.searchClasses(query, email, pageable);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().mustRevalidate())
                .body(results);
    }

    private String getEmailFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            return oAuth2User.getAttribute("email");
        } else if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }
}
