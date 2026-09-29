package com.contentos.modules.discovery.repository;

import com.contentos.modules.discovery.entity.ContentIdea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentIdeaRepository extends JpaRepository<ContentIdea, UUID> {

    // Paginated list of ideas in a workspace (sorted by totalScore DESC by default)
    Page<ContentIdea> findByWorkspaceId(UUID workspaceId, Pageable pageable);

    // Filter by status (e.g. only DISCOVERED, only SELECTED)
    Page<ContentIdea> findByWorkspaceIdAndStatus(
        UUID workspaceId,
        ContentIdea.IdeaStatus status,
        Pageable pageable
    );

    // Ideas belonging to a specific niche
    List<ContentIdea> findByWorkspaceIdAndNicheId(UUID workspaceId, UUID nicheId);

    // Security check: does idea belong to this workspace?
    Optional<ContentIdea> findByIdAndWorkspaceId(UUID id, UUID workspaceId);

    // Count ideas by status in workspace (for dashboard stats)
    long countByWorkspaceIdAndStatus(UUID workspaceId, ContentIdea.IdeaStatus status);
}
