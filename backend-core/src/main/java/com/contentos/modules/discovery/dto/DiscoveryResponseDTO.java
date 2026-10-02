package com.contentos.modules.discovery.dto;

import lombok.*;

/**
 * WHAT IT DOES:
 * Response from AI Engine when discovery is initiated.
 *
 * WHY IT EXISTS:
 * FastAPI returns 202 Accepted immediately with this payload.
 * Actual results come later via webhook.
 *
 * HOW IT CONNECTS:
 * AI Engine POST /api/v1/discovery/generate → This DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscoveryResponseDTO {
    private String status;
    private String workspaceId;
}
