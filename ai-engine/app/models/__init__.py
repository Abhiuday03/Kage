"""
Models Module Initialization
"""

from app.models.domain import (
    DiscoveryRequest,
    DiscoveryResponse,
    ScoringRequest,
    ScoringResponse,
    ContentIdeaPayload,
    DiscoveryWebhookPayload,
    ErrorResponse,
)
from app.models.llm_schemas import (
    GeneratedIdea,
    IdeaGenerationOutput,
    IdeaScoreOutput,
)

__all__ = [
    # Domain models
    "DiscoveryRequest",
    "DiscoveryResponse",
    "ScoringRequest",
    "ScoringResponse",
    "ContentIdeaPayload",
    "DiscoveryWebhookPayload",
    "ErrorResponse",
    # LLM schemas
    "GeneratedIdea",
    "IdeaGenerationOutput",
    "IdeaScoreOutput",
]
