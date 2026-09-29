package com.contentos.modules.discovery.controller;

import com.contentos.common.response.ApiResponse;
import com.contentos.modules.discovery.dto.NicheDTO;
import com.contentos.modules.discovery.service.NicheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * WHAT IT DOES:
 * REST endpoints for managing niches (topic areas) within a workspace.
 *
 * WHY IT EXISTS:
 * Users create and manage their content niches through this API.
 *
 * HOW IT CONNECTS:
 * Frontend Niche settings view calls these endpoints.
 * Automatically tenant-scoped via workspaceId path variable.
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/niches")
@Tag(name = "Niches", description = "Niche and topic monitoring management")
@Slf4j
public class NicheController {

    private final NicheService nicheService;

    public NicheController(NicheService nicheService) {
        this.nicheService = nicheService;
    }

    @GetMapping
    @Operation(summary = "List niches", description = "Get all niches monitored in a workspace")
    public ResponseEntity<ApiResponse<List<NicheDTO>>> getNiches(
        @PathVariable UUID workspaceId
    ) {
        List<NicheDTO> niches = nicheService.getNiches(workspaceId);
        return ResponseEntity.ok(ApiResponse.success(niches));
    }

    @PostMapping
    @Operation(summary = "Create niche", description = "Add a new niche to monitor in a workspace")
    public ResponseEntity<ApiResponse<NicheDTO>> createNiche(
        @PathVariable UUID workspaceId,
        @Valid @RequestBody NicheDTO dto
    ) {
        log.info("Creating niche: {} in workspace: {}", dto.getName(), workspaceId);
        NicheDTO created = nicheService.createNiche(workspaceId, dto);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/{nicheId}")
    @Operation(summary = "Update niche", description = "Update an existing niche definition")
    public ResponseEntity<ApiResponse<NicheDTO>> updateNiche(
        @PathVariable UUID workspaceId,
        @PathVariable UUID nicheId,
        @Valid @RequestBody NicheDTO dto
    ) {
        log.info("Updating niche: {} in workspace: {}", nicheId, workspaceId);
        NicheDTO updated = nicheService.updateNiche(workspaceId, nicheId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{nicheId}")
    @Operation(summary = "Delete niche", description = "Delete a niche from a workspace")
    public ResponseEntity<ApiResponse<Void>> deleteNiche(
        @PathVariable UUID workspaceId,
        @PathVariable UUID nicheId
    ) {
        log.info("Deleting niche: {} from workspace: {}", nicheId, workspaceId);
        nicheService.deleteNiche(workspaceId, nicheId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
