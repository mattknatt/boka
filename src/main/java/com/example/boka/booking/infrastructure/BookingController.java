package com.example.boka.booking.infrastructure;

import com.example.boka.booking.application.BookingRequest;
import com.example.boka.booking.application.BookingResponse;
import com.example.boka.booking.application.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/my")
    public ResponseEntity<?> getUserBookings(Authentication authentication) {
        String email = getEmailFromAuthentication(authentication);
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User must be logged in to view bookings"));
        }

        return ResponseEntity.ok(bookingService.getUserBookings(email));
    }

    @PostMapping
    public ResponseEntity<?> createBooking(
            @Valid @RequestBody BookingRequest request,
            BindingResult bindingResult,
            Authentication authentication
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        String email = getEmailFromAuthentication(authentication);
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User must be logged in to book a class"));
        }

        try {
            BookingResponse response = bookingService.createBooking(request.gymClassId(), email);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{gymClassId}")
    public ResponseEntity<?> deleteBooking(
            @PathVariable Long gymClassId,
            Authentication authentication
    ) {
        String email = getEmailFromAuthentication(authentication);
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User must be logged in to cancel a booking"));
        }

        try {
            bookingService.cancelBooking(gymClassId, email);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
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
