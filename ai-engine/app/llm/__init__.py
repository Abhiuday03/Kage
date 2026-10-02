"""
LLM Module Initialization

This file makes the llm package easily importable and provides factory functions.
"""

from app.llm.provider import LLMProvider
from app.llm.openai_provider import OpenAIProvider

# Default provider instance (singleton pattern)
_default_provider: LLMProvider | None = None


def get_llm_provider() -> LLMProvider:
    """
    Get the default LLM provider instance.

    Uses lazy initialization to ensure config is loaded.

    Returns:
        LLMProvider: The active LLM provider (currently OpenAI)
    """
    global _default_provider
    if _default_provider is None:
        _default_provider = OpenAIProvider()
    return _default_provider


__all__ = ["LLMProvider", "OpenAIProvider", "get_llm_provider"]
