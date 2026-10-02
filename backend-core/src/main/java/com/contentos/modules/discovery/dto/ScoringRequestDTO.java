package com.contentos.modules.discovery.dto;

import lombok.*;

import java.util.List;

/**
 * WHAT IT DOES:
 * Request to score a single content idea.
 *
 * WHY IT EXISTS:
 * Sent to AI Engine to evaluate an idea across trend, relevance, and engagement.
 * Maps to FastAPI's ScoringRequest model.
 *
 * HOW IT CONNECTS:
 * ContentIdeaService → AI Engine POST /api/v1/scoring/evaluate
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringRequestDTO {
    private String ideaId;
    private String title;
    private String description;
    private List<String> nicheKeywords;
}
