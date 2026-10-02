package com.contentos.modules.discovery.controller;

import com.contentos.common.response.ApiResponse;
import com.contentos.modules.discovery.dto.DiscoveryResponseDTO;
import com.contentos.modules.discovery.service.DiscoveryService;
import com.contentos.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * WHAT IT DOES:
 * User-facing endpoint to trigger topic discovery for a niche.
 *
 * WHY IT EXISTS:
 * Frontend uses this to start the discovery process for a workspace's niche.
 * Returns 202 Accepted immediately. AI Engine processes async and webhooks results.
 *
 * HOW IT CONNECTS:
 * Frontend → TopicDiscoveryController → DiscoveryService → AiEngineClient → FastAPI
 * FastAPI processes and webhooks back → InternalWebhookController
 */
@RestController
@RequestMapping("/api/v1/discovery")
@Tag(name = "Discovery", description = "Endpoints for triggering content idea discovery")
@Slf4j
public class TopicDiscoveryController {

    private final DiscoveryService discoveryService;
    private final TenantContext tenantContext;

    public TopicDiscoveryController(DiscoveryService discoveryService, TenantContext tenantContext) {
        this.discoveryService = discoveryService;
        this.tenantContext = tenantContext;
    }

    /**
     * POST /api/v1/discovery/trigger
     *
     * Trigger async idea discovery for a specific niche.
     * Returns 202 Accepted immediately.
     * Results arrive later via webhook.
     */
    @PostMapping("/trigger")
    @Operation(
        summary = "Trigger idea discovery",
        description = "Initiate async discovery process for a niche. Results sent via webhook."
    )
    public ResponseEntity<ApiResponse<DiscoveryResponseDTO>> triggerDiscovery(
        @Parameter(description = "Niche ID") @RequestParam UUID nicheId,
        @Parameter(description = "Number of ideas to generate") @RequestParam(required = false) Integer count
    ) {
        UUID workspaceId = tenantContext.getCurrentWorkspaceId();
        log.info("Discovery triggered for niche: {} in workspace: {}", nicheId, workspaceId);

        DiscoveryResponseDTO response = discoveryService.triggerDiscovery(workspaceId, nicheId, count);

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(response));
    }
}