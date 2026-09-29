package com.contentos.modules.workspace.entity;

import com.contentos.modules.identity.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WHAT IT DOES:
 * Represents a workspace - the operational boundary for all content, brands, and media.
 * Example: "Tech Blog", "Agency Client X", "Personal Brand"
 *
 * WHY IT EXISTS:
 * Workspaces provide operational isolation within an organization. All content, ideas,
 * media assets, and campaigns belong to exactly one workspace. This is the tenant
 * boundary that appears in every major table as workspace_id.
 *
 * HOW IT CONNECTS:
 * Workspace.organization_id → Organization
 * Workspace → BrandProfile (1:1)
 * Workspace → Niches (1:many)
 * Workspace → ContentIdeas (1:many)
 * Every request includes X-Workspace-ID header that references this table.
 */
@Entity
@Table(name = "workspaces",
    indexes = {
        @Index(name = "idx_workspaces_org", columnList = "organization_id"),
        @Index(name = "idx_workspaces_slug", columnList = "slug")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_org_workspace_slug", columnNames = {"organization_id", "slug"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}