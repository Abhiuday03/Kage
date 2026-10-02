package com.contentos.modules.discovery.dto;

import com.contentos.modules.discovery.entity.ContentIdea;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WHAT IT DOES:
 * Public API representation of a ContentIdea.
 *
 * WHY IT EXISTS:
 * DTOs prevent exposing internal entity structure to the frontend.
 * This is what the React dashboard receives when fetching ideas.
 *
 * HOW IT CONNECTS:
 * ContentIdeaService converts ContentIdea entities to this DTO.
 * DiscoveryController returns lists of these.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentIdeaDTO {
    private UUID id;
    private UUID workspaceId;
    private UUID nicheId;
    private String title;
    private String description;
    private String sourceUrl;

    private BigDecimal trendScore;
    private BigDecimal relevanceScore;
    private BigDecimal engagementScore;
    private BigDecimal totalScore;

    private ContentIdea.IdeaStatus status;
    private LocalDateTime discoveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
