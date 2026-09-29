package com.contentos.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * WHAT IT DOES:
 * Spring Security's representation of an authenticated user. Implements UserDetails
 * so Spring Security can work with our User entity.
 *
 * WHY IT EXISTS:
 * Spring Security needs a UserDetails object for every authenticated request.
 * This wraps our User entity and provides the interface Spring Security expects.
 *
 * HOW IT CONNECTS:
 * Created by CustomUserDetailsService when loading a user from the database.
 * Stored in SecurityContextHolder after successful authentication.
 * Retrieved via SecurityContextHolder.getContext().getAuthentication() in controllers.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final String fullName;

    public UserPrincipal(UUID id, String email, String passwordHash, String fullName) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For now, all authenticated users have ROLE_USER
        // In future phases, we'll add workspace-specific roles
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email; // Email is the username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
