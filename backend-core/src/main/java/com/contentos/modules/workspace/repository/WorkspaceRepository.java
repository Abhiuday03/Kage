package com.contentos.modules.workspace.repository;

import com.contentos.modules.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    // Get all workspaces in an organization
    List<Workspace> findByOrganizationId(UUID organizationId);

    // Get all ACTIVE workspaces in an organization
    List<Workspace> findByOrganizationIdAndIsActiveTrue(UUID organizationId);

    // Find a workspace by its unique slug within an org
    Optional<Workspace> findByOrganizationIdAndSlug(UUID organizationId, String slug);

    // Security check: does a workspace belong to a specific organization?
    boolean existsByIdAndOrganizationId(UUID workspaceId, UUID organizationId);

    // Get workspaces accessible to a user via their organization memberships
    @Query("""
        SELECT w FROM Workspace w
        WHERE w.organization.id IN (
            SELECT m.organization.id FROM OrganizationMember m WHERE m.user.id = :userId
        )
        AND w.isActive = true
        ORDER BY w.createdAt DESC
    """)
    List<Workspace> findAllAccessibleByUserId(@Param("userId") UUID userId);
}
