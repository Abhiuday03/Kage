package com.contentos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * WHAT IT DOES:
 * Provides Spring Security beans (PasswordEncoder, etc.)
 *
 * WHY IT EXISTS:
 * We need to hash passwords before storing them in the database.
 * BCryptPasswordEncoder is industry standard for password hashing.
 *
 * HOW IT CONNECTS:
 * Injected into AuthenticationService for password hashing during registration.
 * Spring Security uses it automatically during password validation.
 */
@Configuration
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
