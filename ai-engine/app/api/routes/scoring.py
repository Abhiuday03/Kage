"""
Scoring Route Handlers

These endpoints handle idea evaluation and scoring.
Scoring is synchronous because it's fast (single idea evaluation).

WHY: Evaluating a single idea takes only a few seconds.
     We can return the result immediately without needing async patterns.

HOW: The scoring endpoint takes an idea and returns scores synchronously.
"""

import logging
from typing import Annotated

from fastapi import APIRouter, Depends, status

from app.api.dependencies import get_llm, require_internal_secret
from app.llm.provider import LLMProvider
from app.models.domain import ScoringRequest, ScoringResponse
from app.services.scoring_engine import ScoringEngine

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/v1/scoring", tags=["scoring"])


@router.post(
    "/evaluate",
    response_model=ScoringResponse,
    status_code=status.HTTP_200_OK,
    dependencies=[require_internal_secret],
    summary="Score a content idea",
    description="Evaluates a content idea across trend, relevance, and engagement dimensions.",
)
async def evaluate_idea(
    request: ScoringRequest,
    llm: Annotated[LLMProvider, Depends(get_llm)],
) -> ScoringResponse:
    """
    Score a content idea across multiple dimensions.

    This endpoint:
    1. Validates the incoming request
    2. Uses the LLM to evaluate the idea
    3. Returns scores and reasoning

    Args:
        request: The idea to score (title, description, niche context)
        llm: Injected LLM provider instance

    Returns:
        ScoringResponse with trend, relevance, engagement, and total scores
    """
    logger.info(f"Scoring idea {request.idea_id}: {request.title}")

    engine = ScoringEngine(llm_provider=llm)
    result = await engine.score_idea(request)

    logger.info(f"Scored idea {request.idea_id} with total score: {result.total_score}")

    return result


@router.get(
    "/health",
    summary="Health check for scoring service",
)
async def health_check() -> dict:
    """
    Simple health check endpoint.

    Returns:
        Status information
    """
    return {"status": "ok", "service": "scoring"}
