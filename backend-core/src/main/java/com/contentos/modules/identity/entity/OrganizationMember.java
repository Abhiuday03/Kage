package com.contentos.modules.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WHAT IT DOES:
 * Join table representing a user's membership in an organization with a role.
 *
 * WHY IT EXISTS:
 * Users can have different roles (OWNER, ADMIN, MEMBER, VIEWER) in different organizations.
 * This table tracks which user has what role in which org.
 *
 * HOW IT CONNECTS:
 * OrganizationMember.user_id → User
 * OrganizationMember.organization_id → Organization
 * Role determines permissions checked in AuthorizationService.
 */
@Entity
@Table(name = "organization_members",
    indexes = {
        @Index(name = "idx_org_members_org", columnList = "organization_id"),
        @Index(name = "idx_org_members_user", columnList = "user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_org_user", columnNames = {"organization_id", "user_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime joinedAt;

    public enum MemberRole {
        OWNER,    // Can manage org, billing, members, delete org
        ADMIN,    // Can manage workspaces, members, settings
        MEMBER,   // Can create/edit content in assigned workspaces
        VIEWER    // Read-only access
    }
}
