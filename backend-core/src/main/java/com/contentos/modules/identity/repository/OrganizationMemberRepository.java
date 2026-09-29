package com.contentos.modules.identity.repository;

import com.contentos.modules.identity.entity.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    // Find the membership for a specific user in a specific organization
    Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    // All memberships for a user (to list their orgs)
    List<OrganizationMember> findByUserId(UUID userId);

    // All members of a specific org
    List<OrganizationMember> findByOrganizationId(UUID organizationId);

    // Check if a user is a member of a given organization
    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    // Find all organization IDs that a user belongs to
    @Query("SELECT m.organization.id FROM OrganizationMember m WHERE m.user.id = :userId")
    List<UUID> findOrganizationIdsByUserId(@Param("userId") UUID userId);
}
