"""
API Module Initialization

This file makes the API utilities easily importable.
"""

from app.api.dependencies import get_llm, require_internal_secret

__all__ = ["get_llm", "require_internal_secret"]
