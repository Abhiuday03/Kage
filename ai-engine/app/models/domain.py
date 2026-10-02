"""
Domain Models (Request/Response Schemas)

These Pydantic models define the shape of data flowing between services.
They provide automatic validation and clear API contracts.

WHY: Type safety and validation prevent bugs at the API boundary.
     These models serve as both documentation and runtime validation.

HOW: FastAPI automatically validates incoming JSON against these schemas.
"""

from typing import List, Optional
from pydantic import BaseModel, Field


# ============================================
# TOPIC DISCOVERY MODELS
# ============================================

class DiscoveryRequest(BaseModel):
    """
    Request to generate content ideas for a workspace.

    This is what Spring Boot sends to initiate idea generation.
    """
    workspace_id: str = Field(..., description="Unique workspace identifier")
    niche_keywords: List[str] = Field(..., min_length=1, description="Keywords defining the content niche")
    target_audience: str = Field(..., description="Description of the target audience")
    count: int = Field(default=5, ge=1, le=20, description="Number of ideas to generate (1-20)")


class DiscoveryResponse(BaseModel):
    """
    Immediate response when generation starts.

    FastAPI returns this instantly, then processes in the background.
    """
    status: str = Field(default="processing", description="Status of the request")
    workspace_id: str = Field(..., description="Workspace that initiated the request")


class ContentIdeaPayload(BaseModel):
    """
    A single generated content idea.

    This is sent back to Spring Boot via webhook.
    """
    title: str = Field(..., description="Content idea title")
    description: str = Field(..., description="Detailed description of the content idea")
    status: str = Field(default="DISCOVERED", description="Initial status")


class DiscoveryWebhookPayload(BaseModel):
    """
    Webhook payload sent back to Spring Boot after generation completes.

    This contains all generated ideas for a workspace.
    """
    workspace_id: str = Field(..., description="Workspace identifier")
    ideas: List[ContentIdeaPayload] = Field(..., description="List of generated ideas")


# ============================================
# IDEA SCORING MODELS
# ============================================

class ScoringRequest(BaseModel):
    """
    Request to score a single content idea.

    Spring Boot sends this when a user wants to evaluate an idea.
    """
    idea_id: str = Field(..., description="Unique identifier for the idea")
    title: str = Field(..., description="Content idea title")
    description: str = Field(..., description="Content idea description")
    niche_keywords: List[str] = Field(..., description="Niche context for relevance scoring")


class ScoringResponse(BaseModel):
    """
    Scoring result with individual and total scores.

    Returned synchronously to Spring Boot.
    """
    idea_id: str = Field(..., description="ID of the scored idea")
    trend_score: int = Field(..., ge=0, le=100, description="Trend relevance (0-100)")
    relevance_score: int = Field(..., ge=0, le=100, description="Niche relevance (0-100)")
    engagement_score: int = Field(..., ge=0, le=100, description="Predicted engagement (0-100)")
    total_score: float = Field(..., ge=0, le=100, description="Weighted average score")
    scoring_reasoning: Optional[str] = Field(None, description="Explanation of the score")


# ============================================
# ERROR MODELS
# ============================================

class ErrorResponse(BaseModel):
    """
    Standard error response format.
    """
    error: str = Field(..., description="Error type")
    message: str = Field(..., description="Human-readable error message")
    details: Optional[dict] = Field(None, description="Additional error context")
