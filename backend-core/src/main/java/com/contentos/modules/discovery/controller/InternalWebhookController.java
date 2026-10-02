package com.contentos.modules.discovery.controller;

import com.contentos.common.response.ApiResponse;
import com.contentos.modules.discovery.dto.DiscoveryWebhookPayloadDTO;
import com.contentos.modules.discovery.service.ContentIdeaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * WHAT IT DOES:
 * Receives webhook callbacks from the AI Engine FastAPI service.
 * This is an INTERNAL endpoint (not for frontend use).
 *
 * WHY IT EXISTS:
 * The AI Engine processes discovery asynchronously and calls us back
 * when ready with generated ideas. This controller validates the request
 * and saves the ideas to the database.
 *
 * HOW IT CONNECTS:
 * AI Engine → InternalWebhookController → ContentIdeaService → Database
 *
 * SECURITY:
 * This endpoint validates the X-Internal-Secret header to ensure
 * the request actually came from our AI Engine (not an attacker).
 */
@RestController
@RequestMapping("/api/internal/webhooks")
@Tag(name = "Internal Webhooks", description = "Internal endpoints for service-to-service communication")
@Slf4j
public class InternalWebhookController {

    private final ContentIdeaService contentIdeaService;
    private final String webhookSecret;

    public InternalWebhookController(
        ContentIdeaService contentIdeaService,
        @Value("${app.ai-engine.webhook-secret}") String webhookSecret
    ) {
        this.contentIdeaService = contentIdeaService;
        this.webhookSecret = webhookSecret;
    }

    /**
     * POST /api/internal/webhooks/ideas
     *
     * Receive generated ideas from AI Engine.
     *
     * This endpoint:
     * 1. Validates the X-Internal-Secret header
     * 2. Extracts ideas from the webhook payload
     * 3. Saves them to the database
     * 4. Returns 200 OK
     *
     * The AI Engine calls this after generating ideas for a workspace.
     */
    @PostMapping("/ideas")
    @Operation(
        summary = "Receive generated ideas from AI Engine",
        description = "Internal webhook endpoint. Called by AI Engine with generated content ideas."
    )
    public ResponseEntity<ApiResponse<Void>> receiveGeneratedIdeas(
        @RequestHeader(value = "X-Internal-Secret", required = false) String providedSecret,
        @RequestBody DiscoveryWebhookPayloadDTO payload
    ) {
        // Validate internal secret
        if (!isValidSecret(providedSecret)) {
            log.warn("Unauthorized webhook request: invalid or missing secret");
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Invalid internal secret", "Unauthorized"));
        }

        log.info("Received ideas webhook for workspace: {}", payload.getWorkspaceId());

        try {
            // Parse workspace ID from string
            UUID workspaceId = UUID.fromString(payload.getWorkspaceId());

            // Save ideas to database
            contentIdeaService.createIdeasFromWebhook(workspaceId, payload.getIdeas());

            log.info("Successfully processed {} ideas for workspace {}",
                payload.getIdeas().size(), workspaceId);

            return ResponseEntity.ok(
                ApiResponse.success(null, "Ideas received and saved successfully")
            );

        } catch (IllegalArgumentException e) {
            log.error("Invalid workspace ID format: {}", payload.getWorkspaceId());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid workspace ID", e.getMessage()));

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Webhook processing failed", e.getMessage()));
        }
    }

    /**
     * Validate the internal API secret.
     *
     * This ensures the webhook request actually came from our AI Engine
     * and not from an attacker.
     */
    private boolean isValidSecret(String providedSecret) {
        if (providedSecret == null || providedSecret.isBlank()) {
            return false;
        }
        return providedSecret.equals(webhookSecret);
    }
}
