package com.contentos.modules.identity.service;

import com.contentos.common.exception.ConflictException;
import com.contentos.modules.identity.dto.AuthResponse;
import com.contentos.modules.identity.dto.LoginRequest;
import com.contentos.modules.identity.dto.RegisterRequest;
import com.contentos.modules.identity.entity.Organization;
import com.contentos.modules.identity.entity.OrganizationMember;
import com.contentos.modules.identity.entity.User;
import com.contentos.modules.identity.repository.OrganizationRepository;
import com.contentos.modules.identity.repository.UserRepository;
import com.contentos.modules.identity.repository.OrganizationMemberRepository;
import com.contentos.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Optional;

/**
 * WHAT IT DOES:
 * Handles user registration and login. Creates new users, validates passwords,
 * generates JWT tokens, and creates their first organization.
 *
 * WHY IT EXISTS:
 * Centralizes authentication logic so controllers stay thin. Handles password
 * encoding, token generation, and duplicate-prevention.
 *
 * HOW IT CONNECTS:
 * AuthController calls this service for /register and /login endpoints.
 * Returns AuthResponse with JWT token for successful auth.
 */
@Service
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticationService(
        UserRepository userRepository,
        OrganizationRepository organizationRepository,
        OrganizationMemberRepository organizationMemberRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Register a new user and create their first organization.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate that email doesn't already exist
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        // Create the user
        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .emailVerified(false)
            .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        // Create their first organization
        String orgSlug = generateSlug(request.getOrganizationName());
        Organization org = Organization.builder()
            .name(request.getOrganizationName())
            .slug(orgSlug)
            .billingTier("FREE")
            .monthlyCreditsLimit(100)
            .monthlyCreditsUsed(0)
            .build();

        org = organizationRepository.save(org);
        log.info("New organization created: {} ({})", org.getName(), org.getSlug());

        // Add user as OWNER of the organization
        OrganizationMember membership = OrganizationMember.builder()
            .organization(org)
            .user(user)
            .role(OrganizationMember.MemberRole.OWNER)
            .build();

        organizationMemberRepository.save(membership);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return buildAuthResponse(token, user, org);
    }

    /**
     * Authenticate a user with email and password.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = userOpt.get();

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for user: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Get the user's first organization
        Optional<OrganizationMember> membership = organizationMemberRepository.findByUserId(user.getId())
            .stream()
            .findFirst();

        if (membership.isEmpty()) {
            throw new IllegalStateException("User has no organization membership");
        }

        Organization org = membership.get().getOrganization();

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(token, user, org);
    }

    /**
     * Build AuthResponse from user and organization.
     */
    private AuthResponse buildAuthResponse(String token, User user, Organization org) {
        return AuthResponse.builder()
            .token(token)
            .user(AuthResponse.UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build())
            .organization(AuthResponse.OrganizationDTO.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .build())
            .build();
    }

    /**
     * Generate URL-safe slug from organization name.
     * Example: "My Company" → "my-company"
     */
    private String generateSlug(String name) {
        String slug = name.toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");

        // Ensure it doesn't already exist
        String finalSlug = slug;
        int counter = 1;
        while (organizationRepository.existsBySlug(finalSlug)) {
            finalSlug = slug + "-" + counter;
            counter++;
        }

        return finalSlug;
    }
}
