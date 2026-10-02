package com.contentos.modules.discovery.dto;

import lombok.*;

import java.util.List;

/**
 * WHAT IT DOES:
 * Webhook payload received from AI Engine after idea generation completes.
 *
 * WHY IT EXISTS:
 * FastAPI calls our /api/internal/webhooks/ideas endpoint with this payload
 * to deliver generated content ideas.
 *
 * HOW IT CONNECTS:
 * AI Engine → InternalWebhookController → This DTO → ContentIdeaService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscoveryWebhookPayloadDTO {
    private String workspaceId;
    private List<ContentIdeaPayloadDTO> ideas;
}
