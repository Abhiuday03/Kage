package com.contentos.modules.brand.repository;

import com.contentos.modules.brand.entity.BrandProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandProfileRepository extends JpaRepository<BrandProfile, UUID> {
    Optional<BrandProfile> findByWorkspaceId(UUID workspaceId);
    boolean existsByWorkspaceId(UUID workspaceId);
}
