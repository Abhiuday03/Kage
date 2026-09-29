package com.contentos.modules.brand.controller;

import com.contentos.common.response.ApiResponse;
import com.contentos.modules.brand.dto.BrandProfileDTO;
import com.contentos.modules.brand.service.BrandProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * WHAT IT DOES:
 * REST endpoints for managing brand profile settings for a workspace.
 *
 * WHY IT EXISTS:
 * Users configure brand guidelines (tone, colors, audience, rules) through this API.
 *
 * HOW IT CONNECTS:
 * Frontend Brand Settings page calls these endpoints.
 * All endpoints are automatically scoped to the workspace via path variable and TenantContextFilter.
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/brand")
@Tag(name = "Brand Profile", description = "Brand guidelines and style management")
@Slf4j
public class BrandController {

    private final BrandProfileService brandProfileService;

    public BrandController(BrandProfileService brandProfileService) {
        this.brandProfileService = brandProfileService;
    }

    @GetMapping
    @Operation(summary = "Get brand profile", description = "Get brand guidelines for a workspace")
    public ResponseEntity<ApiResponse<BrandProfileDTO>> getBrandProfile(
        @PathVariable UUID workspaceId
    ) {
        BrandProfileDTO profile = brandProfileService.getOrCreateBrandProfile(workspaceId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping
    @Operation(summary = "Update brand profile", description = "Update brand guidelines for a workspace")
    public ResponseEntity<ApiResponse<BrandProfileDTO>> updateBrandProfile(
        @PathVariable UUID workspaceId,
        @RequestBody BrandProfileDTO dto
    ) {
        log.info("Updating brand profile for workspace: {}", workspaceId);
        BrandProfileDTO updated = brandProfileService.updateBrandProfile(workspaceId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
}
