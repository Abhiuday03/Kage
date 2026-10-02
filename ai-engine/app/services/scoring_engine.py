"""
Scoring Engine Service

This service evaluates content ideas across multiple dimensions:
- Trend Score: How timely/trendy is this topic?
- Relevance Score: How well does it align with the niche?
- Engagement Score: What's the predicted audience engagement?

WHY: Separating scoring logic into a service keeps routes clean and testable.
     We can change the scoring algorithm without touching the API.

HOW: Takes an LLMProvider, constructs a prompt, calls the LLM with structured output,
     then calculates a weighted average score.
"""

import logging
from typing import Optional

from app.llm.provider import LLMProvider
from app.models.domain import ScoringRequest, ScoringResponse
from app.models.llm_schemas import IdeaScoreOutput

logger = logging.getLogger(__name__)


class ScoringEngine:
    """
    Evaluates content ideas and produces scores.

    Uses an LLM to assess trendiness, relevance, and engagement potential.
    """

    def __init__(self, llm_provider: LLMProvider):
        """
        Initialize the scoring engine.

        Args:
            llm_provider: LLMProvider instance for generating scores
        """
        self.llm = llm_provider

    async def score_idea(self, request: ScoringRequest) -> ScoringResponse:
        """
        Score a single content idea.

        Args:
            request: ScoringRequest with idea details and niche context

        Returns:
            ScoringResponse with individual and total scores

        Raises:
            ValueError: If LLM fails to produce valid scores
            TimeoutError: If LLM request times out
        """
        logger.info(f"Scoring idea: {request.title}")

        # Build the prompt for the LLM
        prompt = self._build_scoring_prompt(request)

        # Call the LLM with structured output
        try:
            llm_output = await self.llm.generate_structured(
                prompt=prompt,
                response_schema=IdeaScoreOutput,
                temperature=0.3,  # Lower temperature for consistent scoring
            )
        except (ValueError, TimeoutError) as e:
            logger.error(f"Failed to score idea {request.idea_id}: {e}")
            raise

        # Calculate weighted total score
        total_score = self._calculate_total_score(
            trend=llm_output.trend_score,
            relevance=llm_output.relevance_score,
            engagement=llm_output.engagement_score,
        )

        response = ScoringResponse(
            idea_id=request.idea_id,
            trend_score=llm_output.trend_score,
            relevance_score=llm_output.relevance_score,
            engagement_score=llm_output.engagement_score,
            total_score=total_score,
            scoring_reasoning=llm_output.reasoning,
        )

        logger.info(
            f"Scored idea {request.idea_id}: "
            f"trend={llm_output.trend_score}, "
            f"relevance={llm_output.relevance_score}, "
            f"engagement={llm_output.engagement_score}, "
            f"total={total_score:.1f}"
        )

        return response

    def _build_scoring_prompt(self, request: ScoringRequest) -> str:
        """
        Build the prompt for idea scoring.

        Args:
            request: ScoringRequest with idea and niche data

        Returns:
            Formatted prompt string
        """
        niche_str = ", ".join(request.niche_keywords)

        prompt = f"""You are an expert content strategist evaluating a content idea for a specific niche.

Content Idea:
Title: {request.title}
Description: {request.description}

Target Niche: {niche_str}

Evaluate this idea across three dimensions:

1. **Trend Score (0-100)**: How timely and trending is this topic RIGHT NOW?
   - High score if this is a current hot topic people are searching for
   - Low score if it's outdated or niche

2. **Relevance Score (0-100)**: How well does this align with the target niche?
   - High score if it directly addresses the niche keywords
   - Low score if it's tangential or irrelevant

3. **Engagement Score (0-100)**: What's the predicted audience engagement?
   - High score if this will generate discussion, shares, and comments
   - Low score if it's likely to be overlooked

Provide scores and brief reasoning for each. Be objective and data-driven."""

        return prompt

    @staticmethod
    def _calculate_total_score(
        trend: int, relevance: int, engagement: int, weights: Optional[dict] = None
    ) -> float:
        """
        Calculate weighted average score.

        Default weights prioritize relevance, then trend, then engagement.

        Args:
            trend: Trend score (0-100)
            relevance: Relevance score (0-100)
            engagement: Engagement score (0-100)
            weights: Optional dict with "trend", "relevance", "engagement" keys.
                     Defaults to {'relevance': 0.5, 'trend': 0.3, 'engagement': 0.2}

        Returns:
            Weighted average score (0-100)
        """
        if weights is None:
            weights = {
                "relevance": 0.5,  # Most important
                "trend": 0.3,      # Secondary
                "engagement": 0.2, # Tertiary
            }

        total = (
            trend * weights["trend"]
            + relevance * weights["relevance"]
            + engagement * weights["engagement"]
        )

        return round(total, 1)
