package com.contentos.modules.workspace.controller;

import com.contentos.common.response.ApiResponse;
import com.contentos.modules.workspace.dto.CreateWorkspaceRequest;
import com.contentos.modules.workspace.dto.WorkspaceDTO;
import com.contentos.modules.workspace.service.WorkspaceService;
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
 * REST endpoints for workspace management: list, get, create workspaces.
 *
 * WHY IT EXISTS:
 * Users need to switch between workspaces and create new ones.
 * This controller provides the API for workspace operations.
 *
 * HOW IT CONNECTS:
 * Frontend workspace switcher calls these endpoints.
 * All endpoints require authentication (JWT token).
 * Individual workspace endpoints require workspace-level access via TenantContextFilter.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Workspaces", description = "Workspace management endpoints")
@Slf4j
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping("/workspaces")
    @Operation(summary = "List all workspaces", description = "Get all workspaces accessible to the current user")
    public ResponseEntity<ApiResponse<List<WorkspaceDTO>>> getAllWorkspaces() {
        List<WorkspaceDTO> workspaces = workspaceService.getAllWorkspaces();
        return ResponseEntity.ok(ApiResponse.success(workspaces));
    }

    @GetMapping("/workspaces/{workspaceId}")
    @Operation(summary = "Get workspace details", description = "Get detailed information about a specific workspace")
    public ResponseEntity<ApiResponse<WorkspaceDTO>> getWorkspace(
        @PathVariable UUID workspaceId
    ) {
        WorkspaceDTO workspace = workspaceService.getWorkspace(workspaceId);
        return ResponseEntity.ok(ApiResponse.success(workspace));
    }

    @PostMapping("/workspaces")
    @Operation(summary = "Create new workspace", description = "Create a new workspace in the user's organization")
    public ResponseEntity<ApiResponse<WorkspaceDTO>> createWorkspace(
        @Valid @RequestBody CreateWorkspaceRequest request
    ) {
        log.info("Creating workspace: {}", request.getName());
        WorkspaceDTO workspace = workspaceService.createWorkspace(request);
        return ResponseEntity.ok(ApiResponse.success(workspace));
    }
}