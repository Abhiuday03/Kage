package com.contentos.modules.discovery.controller;

import com.contentos.common.response.ApiResponse;
import com.contentos.modules.discovery.dto.ContentIdeaDTO;
import com.contentos.modules.discovery.entity.ContentIdea;
import com.contentos.modules.discovery.service.ContentIdeaService;
import com.contentos.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * WHAT IT DOES:
 * REST endpoints for managing content ideas (viewing, filtering, scoring, deleting).
 *
 * WHY IT EXISTS:
 * The React frontend uses these endpoints to:
 * - Fetch ideas by status (DISCOVERED, SCORED, SELECTED, etc.)
 * - Score individual ideas
 * - Update idea status
 * - Build the Kanban board
 *
 * HOW IT CONNECTS:
 * Frontend → DiscoveryController → ContentIdeaService
 * All endpoints are tenant-aware (workspace_id from TenantContext)
 */
@RestController
@RequestMapping("/api/v1/ideas")
@Tag(name = "Content Ideas", description = "Endpoints for managing discovered content ideas")
@Slf4j
public class DiscoveryController {

    private final ContentIdeaService contentIdeaService;

    public DiscoveryController(ContentIdeaService contentIdeaService) {
        this.contentIdeaService = contentIdeaService;
    }

    /**
     * GET /api/v1/ideas
     *
     * Retrieve paginated list of ideas for the current workspace.
     * Sorted by total score (highest first).
     */
    @GetMapping
    @Operation(
        summary = "Get all ideas",
        description = "Fetch paginated list of content ideas for the workspace"
    )
    public ResponseEntity<ApiResponse<Page<ContentIdeaDTO>>> getIdeas(
        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        UUID workspaceId = TenantContext.getCurrentWorkspaceId();
        log.info("Fetching ideas for workspace: {}, page: {}, size: {}", workspaceId, page, size);

        Page<ContentIdeaDTO> ideas = contentIdeaService.getIdeas(workspaceId, page, size);

        return ResponseEntity.ok(ApiResponse.success(ideas, "Ideas retrieved successfully"));
    }

    /**
     * GET /api/v1/ideas/status/{status}
     *
     * Retrieve ideas filtered by status (e.g., DISCOVERED, SCORED, SELECTED).
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get ideas by status",
        description = "Fetch paginated list of ideas filtered by status"
    )
    public ResponseEntity<ApiResponse<Page<ContentIdeaDTO>>> getIdeasByStatus(
        @Parameter(description = "Idea status (DISCOVERED, SCORED, SELECTED, BRIEFED, etc.)")
        @PathVariable ContentIdea.IdeaStatus status,
        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        UUID workspaceId = TenantContext.getCurrentWorkspaceId();
        log.info("Fetching ideas with status: {} for workspace: {}", status, workspaceId);

        Page<ContentIdeaDTO> ideas = contentIdeaService.getIdeasByStatus(workspaceId, status, page, size);

        return ResponseEntity.ok(ApiResponse.success(ideas, "Ideas retrieved successfully"));
    }

    /**
     * GET /api/v1/ideas/{ideaId}
     *
     * Retrieve a single idea by ID.
     */
    @GetMapping("/{ideaId}")
    @Operation(summary = "Get a specific idea")
    public ResponseEntity<ApiResponse<ContentIdeaDTO>> getIdea(
        @Parameter(description = "Idea ID") @PathVariable UUID ideaId
    ) {
        UUID workspaceId = TenantContext.getCurrentWorkspaceId();
        log.info("Fetching idea: {} for workspace: {}", ideaId, workspaceId);

        ContentIdeaDTO idea = contentIdeaService.getIdea(workspaceId, ideaId);

        return ResponseEntity.ok(ApiResponse.success(idea, "Idea retrieved successfully"));
    }

    /**
     * POST /api/v1/ideas/{ideaId}/score
     *
     * Score an idea using the AI Engine.
     * Requires the niche ID to provide context for scoring.
     */
    @PostMapping("/{ideaId}/score")
    @Operation(
        summary = "Score an idea",
        description = "Evaluate a content idea for trend, relevance, and engagement"
    )
    public ResponseEntity<ApiResponse<ContentIdeaDTO>> scoreIdea(
        @Parameter(description = "Idea ID") @PathVariable UUID ideaId,
        @Parameter(description = "Niche ID for context") @RequestParam UUID nicheId
    ) {
        UUID workspaceId = TenantContext.getCurrentWorkspaceId();
        log.info("Scoring idea: {} with niche: {} for workspace: {}", ideaId, nicheId, workspaceId);

        ContentIdeaDTO scoredIdea = contentIdeaService.scoreIdea(workspaceId, ideaId, nicheId);

        return ResponseEntity.ok(ApiResponse.success(scoredIdea, "Idea scored successfully"));
    }

    /**
     * PATCH /api/v1/ideas/{ideaId}/status
     *
     * Update the status of an idea (e.g., SELECTED, REJECTED).
     */
    @PatchMapping("/{ideaId}/status")
    @Operation(summary = "Update idea status")
    public ResponseEntity<ApiResponse<ContentIdeaDTO>> updateIdeaStatus(
        @Parameter(description = "Idea ID") @PathVariable UUID ideaId,
        @Parameter(description = "New status") @RequestParam ContentIdea.IdeaStatus status
    ) {
        UUID workspaceId = TenantContext.getCurrentWorkspaceId();
        log.info("Updating idea: {} status to: {} for workspace: {}", ideaId, status, workspaceId);

        ContentIdeaDTO updatedIdea = contentIdeaService.updateIdeaStatus(workspaceId, ideaId, status);

        return ResponseEntity.ok(ApiResponse.success(updatedIdea, "Idea status updated successfully"));
    }

    /**
     * DELETE /api/v1/ideas/{ideaId}
     *
     * Delete an idea.
     */
    @DeleteMapping("/{ideaId}")
    @Operation(summary = "Delete an idea")
    public ResponseEntity<ApiResponse<Void>> deleteIdea(
        @Parameter(description = "Idea ID") @PathVariable UUID ideaId
    ) {
        UUID workspaceId = TenantContext.getCurrentWorkspaceId();
        log.info("Deleting idea: {} for workspace: {}", ideaId, workspaceId);

        contentIdeaService.deleteIdea(workspaceId, ideaId);

        return ResponseEntity.ok(ApiResponse.success(null, "Idea deleted successfully"));
    }
}
