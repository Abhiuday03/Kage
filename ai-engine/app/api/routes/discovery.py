"""
Discovery Route Handlers

These endpoints handle topic/idea generation for a workspace.
Endpoints use an async webhook pattern to avoid blocking HTTP requests.

WHY: LLM generation takes 5-30 seconds. We don't want to keep HTTP connections
     open that long. Instead, we return 202 Accepted and process in the background.

HOW: FastAPI's BackgroundTasks run after the response is sent.
     When done, we POST the results back to Spring Boot via webhook.
"""

import logging
from typing import Annotated

from fastapi import APIRouter, BackgroundTasks, Depends, status

from app.api.dependencies import get_llm, require_internal_secret
from app.llm.provider import LLMProvider
from app.models.domain import (
    DiscoveryRequest,
    DiscoveryResponse,
    DiscoveryWebhookPayload,
    ContentIdeaPayload,
)
from app.services.discovery_agent import DiscoveryAgent

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/v1/discovery", tags=["discovery"])


@router.post(
    "/generate",
    response_model=DiscoveryResponse,
    status_code=status.HTTP_202_ACCEPTED,
    dependencies=[require_internal_secret],
    summary="Generate content ideas for a workspace",
    description="Initiates async generation of content ideas. Results are sent back via webhook.",
)
async def generate_ideas(
    request: DiscoveryRequest,
    background_tasks: BackgroundTasks,
    llm: Annotated[LLMProvider, Depends(get_llm)],
) -> DiscoveryResponse:
    """
    Generate content ideas for a workspace.

    This endpoint:
    1. Validates the incoming request
    2. Returns 202 Accepted immediately
    3. Schedules background task to generate ideas
    4. Posts results to Spring Boot webhook when done

    Args:
        request: The discovery request with niche keywords and target audience
        background_tasks: FastAPI background task manager
        llm: Injected LLM provider instance

    Returns:
        DiscoveryResponse with processing status
    """
    logger.info(
        f"Discovery request for workspace {request.workspace_id}: "
        f"{len(request.niche_keywords)} keywords, {request.count} ideas requested"
    )

    # Create the discovery agent
    agent = DiscoveryAgent(llm_provider=llm)

    # Schedule the background task
    background_tasks.add_task(
        agent.generate_and_webhook,
        workspace_id=request.workspace_id,
        niche_keywords=request.niche_keywords,
        target_audience=request.target_audience,
        count=request.count,
    )

    logger.info(f"Scheduled background task for workspace {request.workspace_id}")

    return DiscoveryResponse(
        status="processing",
        workspace_id=request.workspace_id,
    )


@router.get(
    "/health",
    summary="Health check for discovery service",
)
async def health_check() -> dict:
    """
    Simple health check endpoint.

    Returns:
        Status information
    """
    return {"status": "ok", "service": "discovery"}
