package com.contentos.modules.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * WHAT IT DOES:
 * Represents a user in the system. One global identity that can belong to multiple organizations.
 *
 * WHY IT EXISTS:
 * Users are the primary authentication boundary. A user can be an owner/admin of multiple
 * organizations and workspaces. This is the "who" in "who is accessing this data?"
 *
 * HOW IT CONNECTS:
 * User → (many) OrganizationMember → Organization
 * Used by Spring Security to load UserDetails and check permissions.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OrganizationMember> organizationMemberships = new HashSet<>();
}
