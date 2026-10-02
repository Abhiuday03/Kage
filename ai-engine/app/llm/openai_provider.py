"""
OpenAI LLM Provider Implementation

This is our concrete implementation for OpenAI's models (GPT-4o, GPT-4 Turbo, etc.).
It inherits from LLMProvider and uses the OpenAI Python SDK.

WHY: We implement the abstract interface so we can swap this out later
     if we want to use Anthropic or a local model.

HOW: We use the OpenAI SDK's structured_outputs feature (available in GPT-4o)
     to ensure the LLM returns valid JSON matching our Pydantic schemas.
"""

import json
import logging
from typing import Type

from pydantic import BaseModel
from openai import AsyncOpenAI, APIError, APITimeoutError

from app.core.config import settings
from app.llm.provider import LLMProvider

logger = logging.getLogger(__name__)


class OpenAIProvider(LLMProvider):
    """
    OpenAI implementation of LLMProvider.

    Uses GPT-4o (or configurable model) with structured outputs.
    """

    def __init__(self):
        """Initialize OpenAI client with the API key from settings."""
        self.client = AsyncOpenAI(api_key=settings.openai_api_key)
        self.model = settings.openai_model

    async def generate_structured(
        self,
        prompt: str,
        response_schema: Type[BaseModel],
        temperature: float = 0.7,
    ) -> BaseModel:
        """
        Generate structured output using OpenAI's structured_outputs feature.

        GPT-4o supports strict JSON schema validation, ensuring the response
        always matches our Pydantic model.

        Args:
            prompt: The system/user prompt
            response_schema: Pydantic model defining expected output
            temperature: Creativity (0.0-1.0)

        Returns:
            Parsed Pydantic instance

        Raises:
            ValueError: If OpenAI fails to generate valid structured output
            TimeoutError: If the request times out
        """
        try:
            # Build the JSON schema from the Pydantic model
            schema = response_schema.model_json_schema()

            logger.info(
                f"Calling OpenAI ({self.model}) with structured output schema: {response_schema.__name__}"
            )

            response = await self.client.beta.chat.completions.parse(
                model=self.model,
                temperature=temperature,
                timeout=settings.llm_timeout,
                messages=[
                    {
                        "role": "user",
                        "content": prompt,
                    }
                ],
                response_format={
                    "type": "json_schema",
                    "json_schema": {
                        "name": response_schema.__name__,
                        "schema": schema,
                        "strict": True,  # Enforce strict schema validation
                    },
                },
            )

            # Extract and parse the response
            parsed = response.choices[0].message.parsed
            if parsed is None:
                raise ValueError("OpenAI returned no parsed content")

            logger.info(f"Successfully generated structured output for {response_schema.__name__}")
            return parsed

        except APITimeoutError as e:
            logger.error(f"OpenAI request timed out: {e}")
            raise TimeoutError(f"LLM request timed out after {settings.llm_timeout}s") from e

        except APIError as e:
            logger.error(f"OpenAI API error: {e}")
            raise ValueError(f"LLM generation failed: {e}") from e

    async def generate_text(
        self,
        prompt: str,
        temperature: float = 0.7,
        max_tokens: int = 2000,
    ) -> str:
        """
        Generate free-form text from OpenAI.

        Args:
            prompt: The user prompt
            temperature: Creativity level
            max_tokens: Maximum response length

        Returns:
            Generated text

        Raises:
            TimeoutError: If the request times out
        """
        try:
            logger.info(f"Calling OpenAI ({self.model}) for text generation")

            response = await self.client.chat.completions.create(
                model=self.model,
                temperature=temperature,
                max_tokens=max_tokens,
                timeout=settings.llm_timeout,
                messages=[
                    {
                        "role": "user",
                        "content": prompt,
                    }
                ],
            )

            text = response.choices[0].message.content or ""
            logger.info(f"Generated {len(text)} characters of text")
            return text

        except APITimeoutError as e:
            logger.error(f"OpenAI request timed out: {e}")
            raise TimeoutError(f"LLM request timed out after {settings.llm_timeout}s") from e

        except APIError as e:
            logger.error(f"OpenAI API error: {e}")
            raise ValueError(f"LLM generation failed: {e}") from e
