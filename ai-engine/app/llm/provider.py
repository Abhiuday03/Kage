"""
LLM Provider Interface (Abstract Base Class)

This module defines the contract that all LLM providers must implement.
By coding to this interface, we can swap providers (OpenAI → Anthropic → Local)
without changing business logic.

WHY: Abstraction prevents vendor lock-in and keeps AI logic testable.
     We can mock this interface in tests or swap to a cheaper model later.

HOW: Each concrete provider (OpenAI, Anthropic, etc.) inherits from LLMProvider
     and implements the abstract methods.
"""

from abc import ABC, abstractmethod
from typing import Type
from pydantic import BaseModel


class LLMProvider(ABC):
    """
    Abstract base class for LLM providers.

    Any LLM provider we integrate with (OpenAI, Anthropic, local Ollama, etc.)
    must implement these methods.
    """

    @abstractmethod
    async def generate_structured(
        self,
        prompt: str,
        response_schema: Type[BaseModel],
        temperature: float = 0.7,
    ) -> BaseModel:
        """
        Generate structured output from the LLM.

        Args:
            prompt: The user prompt/instruction
            response_schema: A Pydantic model defining the expected output structure
            temperature: Creativity level (0.0 = deterministic, 1.0 = creative)

        Returns:
            An instance of response_schema filled by the LLM

        Raises:
            ValueError: If the LLM fails to produce valid structured output
            TimeoutError: If the request exceeds the configured timeout
        """
        pass

    @abstractmethod
    async def generate_text(
        self,
        prompt: str,
        temperature: float = 0.7,
        max_tokens: int = 2000,
    ) -> str:
        """
        Generate free-form text from the LLM.

        Args:
            prompt: The user prompt/instruction
            temperature: Creativity level
            max_tokens: Maximum length of the response

        Returns:
            The generated text

        Raises:
            TimeoutError: If the request exceeds the configured timeout
        """
        pass
