package com.contentos.modules.identity.service;

import com.contentos.common.exception.ConflictException;
import com.contentos.modules.identity.dto.AuthResponse;
import com.contentos.modules.identity.dto.LoginRequest;
import com.contentos.modules.identity.dto.RegisterRequest;
import com.contentos.modules.identity.entity.Organization;
import com.contentos.modules.identity.entity.OrganizationMember;
import com.contentos.modules.identity.entity.User;
import com.contentos.modules.identity.repository.OrganizationMemberRepository;
import com.contentos.modules.identity.repository.OrganizationRepository;
import com.contentos.modules.identity.repository.UserRepository;
import com.contentos.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests authentication business logic in isolation (no database).
 * Mocks all external dependencies so tests are fast.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_shouldCreateUserAndOrganization() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setOrganizationName("Test Org");

        User savedUser = User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .fullName("Test User")
            .passwordHash("hashed")
            .build();

        Organization savedOrg = Organization.builder()
            .id(UUID.randomUUID())
            .name("Test Org")
            .slug("test-org")
            .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(organizationRepository.existsBySlug(anyString())).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenReturn(savedOrg);
        when(organizationMemberRepository.save(any(OrganizationMember.class)))
            .thenReturn(OrganizationMember.builder()
                .organization(savedOrg)
                .user(savedUser)
                .role(OrganizationMember.MemberRole.OWNER)
                .build());
        when(jwtTokenProvider.generateToken(any(UUID.class), anyString())).thenReturn("mock-jwt-token");

        // Act
        AuthResponse response = authenticationService.register(request);

        // Assert
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getOrganization().getName()).isEqualTo("Test Org");

        verify(userRepository).save(any(User.class));
        verify(organizationRepository).save(any(Organization.class));
        verify(organizationMemberRepository).save(any(OrganizationMember.class));
    }

    @Test
    void register_shouldThrowConflictWhenEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setOrganizationName("Test Org");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.register(request))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnTokenForValidCredentials() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        UUID userId = UUID.randomUUID();
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .passwordHash("hashed_password")
            .fullName("Test User")
            .build();

        Organization org = Organization.builder()
            .id(UUID.randomUUID())
            .name("Test Org")
            .slug("test-org")
            .build();

        OrganizationMember membership = OrganizationMember.builder()
            .organization(org)
            .user(user)
            .role(OrganizationMember.MemberRole.OWNER)
            .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(organizationMemberRepository.findByUserId(userId)).thenReturn(List.of(membership));
        when(jwtTokenProvider.generateToken(userId, "test@example.com")).thenReturn("valid-jwt-token");

        // Act
        AuthResponse response = authenticationService.login(request);

        // Assert
        assertThat(response.getToken()).isEqualTo("valid-jwt-token");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void login_shouldThrowForInvalidPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong_password");

        User user = User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .passwordHash("hashed_password")
            .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_shouldThrowForUnknownEmail() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(request))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
