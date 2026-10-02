"""
FastAPI Dependencies for Dependency Injection

This module defines reusable dependencies that FastAPI injects into route handlers.
Dependencies handle cross-cutting concerns like auth, logging, and service initialization.

WHY: Dependencies keep route handlers clean and focused on HTTP concerns only.
     They also make it easy to mock services in tests.

HOW: Define a dependency as an async function, then use it in a route with Depends().
"""

from typing import AsyncGenerator
import logging

from fastapi import Depends

from app.core.security import verify_internal_secret
from app.llm import get_llm_provider
from app.llm.provider import LLMProvider

logger = logging.getLogger(__name__)


async def get_llm() -> LLMProvider:
    """
    Dependency that provides an LLM provider instance.

    This is injected into route handlers that need to call the LLM.
    The same instance is reused across requests (singleton pattern).

    Returns:
        LLMProvider: The active LLM provider (currently OpenAI)

    Example:
        @router.post("/generate")
        async def generate_ideas(llm: LLMProvider = Depends(get_llm)):
            result = await llm.generate_structured(...)
    """
    logger.debug("Providing LLM provider instance")
    return get_llm_provider()


# Security dependency that is reused across routes
require_internal_secret = Depends(verify_internal_secret)

__all__ = ["get_llm", "require_internal_secret"]
