package com.contentos.modules.discovery.dto;

import lombok.*;

import java.util.List;

/**
 * WHAT IT DOES:
 * Request payload to trigger the AI Engine's topic discovery.
 *
 * WHY IT EXISTS:
 * This is sent to the FastAPI service to initiate async idea generation.
 * Maps to FastAPI's DiscoveryRequest model.
 *
 * HOW IT CONNECTS:
 * DiscoveryService → AI Engine POST /api/v1/discovery/generate
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscoveryRequestDTO {
    private String workspaceId;
    private List<String> nicheKeywords;
    private String targetAudience;

    @Builder.Default
    private Integer count = 5;
}
