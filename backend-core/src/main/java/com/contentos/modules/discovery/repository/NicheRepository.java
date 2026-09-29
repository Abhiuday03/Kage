package com.contentos.modules.discovery.repository;

import com.contentos.modules.discovery.entity.Niche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NicheRepository extends JpaRepository<Niche, UUID> {
    List<Niche> findByWorkspaceId(UUID workspaceId);
    List<Niche> findByWorkspaceIdAndIsActiveTrue(UUID workspaceId);
    boolean existsByIdAndWorkspaceId(UUID id, UUID workspaceId);
}
