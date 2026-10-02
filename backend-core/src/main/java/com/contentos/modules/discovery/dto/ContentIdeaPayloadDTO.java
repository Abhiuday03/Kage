package com.contentos.modules.discovery.dto;

import lombok.*;

/**
 * WHAT IT DOES:
 * A single content idea received from the AI Engine webhook.
 *
 * WHY IT EXISTS:
 * Maps to FastAPI's ContentIdeaPayload.
 * Used when AI Engine POSTs generated ideas back to us.
 *
 * HOW IT CONNECTS:
 * Part of DiscoveryWebhookPayloadDTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentIdeaPayloadDTO {
    private String title;
    private String description;
    private String status; // "DISCOVERED"
}
