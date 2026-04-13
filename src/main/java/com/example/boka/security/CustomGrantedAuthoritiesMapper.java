package com.example.boka.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomGrantedAuthoritiesMapper implements GrantedAuthoritiesMapper {

    private final IdentityPort identityPort;

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

        authorities.forEach(authority -> {
            mappedAuthorities.add(authority);

            if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
                Map<String, Object> attributes = oauth2UserAuthority.getAttributes();
                String email = (String) attributes.get("email");

                if (email != null) {
                    identityPort.findByEmail(email).ifPresent(identity -> {
                        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + identity.role()));
                    });
                }
            }
        });

        return mappedAuthorities;
    }
}
