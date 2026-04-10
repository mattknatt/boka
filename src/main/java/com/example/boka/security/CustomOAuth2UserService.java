package com.example.boka.security;

import com.example.boka.entity.AuthProvider;
import com.example.boka.entity.User;
import com.example.boka.entity.UserRole;
import com.example.boka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (!"google".equalsIgnoreCase(registrationId)) {
            throw new OAuth2AuthenticationException("Only Google OAuth2 is supported.");
        }

        String email = (String) attributes.get("email");
        String providerId = (String) attributes.get("sub");
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            updateExistingUser(user, providerId);
        } else {
            user = createNewUser(email, providerId, firstName, lastName);
        }

        // You can return a custom user object here if needed, but for now standard is fine
        return oAuth2User;
    }

    private User createNewUser(String email, String providerId, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName != null ? firstName : "New");
        user.setLastName(lastName != null ? lastName : "User");
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setProviderId(providerId);
        user.setRole(UserRole.MEMBER);
        user.setIsActive(true);
        // passwordHash remains null for OAuth2 users
        return userRepository.save(user);
    }

    private void updateExistingUser(User user, String providerId) {
        if (user.getAuthProvider() == AuthProvider.LOCAL) {
            // Update an existing local user to be linkable to Google if needed
            // Or just set the provider ID if it was missing
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setProviderId(providerId);
            userRepository.save(user);
        } else if (!providerId.equals(user.getProviderId())) {
            user.setProviderId(providerId);
            userRepository.save(user);
        }
    }
}
