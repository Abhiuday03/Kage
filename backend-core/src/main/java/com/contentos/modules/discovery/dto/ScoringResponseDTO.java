package com.contentos.modules.discovery.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * WHAT IT DOES:
 * Scoring result from AI Engine.
 *
 * WHY IT EXISTS:
 * FastAPI returns this synchronously with trend, relevance, and engagement scores.
 * We use these to update the ContentIdea entity.
 *
 * HOW IT CONNECTS:
 * AI Engine POST /api/v1/scoring/evaluate → This DTO → ContentIdea entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringResponseDTO {
    private String ideaId;
    private Integer trendScore;
    private Integer relevanceScore;
    private Integer engagementScore;
    private BigDecimal totalScore;
    private String scoringReasoning;
}
