"""
Discovery Agent Service

This service generates content ideas for a workspace based on niche and audience.
It uses the LLM to brainstorm ideas, then webhooks the results back to Spring Boot.

WHY: Separating the agent logic from routes makes it testable and reusable.
     The agent handles both generation and the webhook callback.

HOW:
1. Build a detailed prompt from niche keywords and target audience
2. Call the LLM with structured output (IdeaGenerationOutput)
3. Convert LLM output to API models
4. POST results back to Spring Boot webhook
"""

import logging
from typing import List

import httpx

from app.core.config import settings
from app.llm.provider import LLMProvider
from app.models.domain import (
    ContentIdeaPayload,
    DiscoveryWebhookPayload,
)
from app.models.llm_schemas import IdeaGenerationOutput

logger = logging.getLogger(__name__)


class DiscoveryAgent:
    """
    Generates content ideas for a workspace.

    Uses an LLM to brainstorm ideas based on niche keywords and target audience,
    then sends results back to Spring Boot via webhook.
    """

    def __init__(self, llm_provider: LLMProvider):
        """
        Initialize the discovery agent.

        Args:
            llm_provider: LLMProvider instance for generating ideas
        """
        self.llm = llm_provider

    async def generate_and_webhook(
        self,
        workspace_id: str,
        niche_keywords: List[str],
        target_audience: str,
        count: int,
    ) -> None:
        """
        Generate ideas and send them back to Spring Boot via webhook.

        This is called as a background task, so it won't block the HTTP response.

        Args:
            workspace_id: The workspace requesting ideas
            niche_keywords: Keywords defining the content niche
            target_audience: Description of the target audience
            count: Number of ideas to generate

        Returns:
            None (sends results via webhook)
        """
        logger.info(
            f"Starting discovery for workspace {workspace_id}: "
            f"keywords={niche_keywords}, audience={target_audience}, count={count}"
        )

        try:
            # Generate the ideas
            ideas = await self._generate_ideas(
                niche_keywords=niche_keywords,
                target_audience=target_audience,
                count=count,
            )

            logger.info(f"Generated {len(ideas)} ideas for workspace {workspace_id}")

            # Send back to Spring Boot via webhook
            await self._send_webhook(workspace_id, ideas)

            logger.info(f"Successfully completed discovery for workspace {workspace_id}")

        except Exception as e:
            logger.error(
                f"Discovery failed for workspace {workspace_id}: {e}",
                exc_info=True,
            )
            # TODO: In production, we'd want to notify Spring Boot of the failure

    async def _generate_ideas(
        self,
        niche_keywords: List[str],
        target_audience: str,
        count: int,
    ) -> List[ContentIdeaPayload]:
        """
        Generate content ideas using the LLM.

        Args:
            niche_keywords: Keywords defining the niche
            target_audience: Target audience description
            count: Number of ideas to generate

        Returns:
            List of ContentIdeaPayload objects

        Raises:
            ValueError: If LLM generation fails
            TimeoutError: If LLM request times out
        """
        prompt = self._build_generation_prompt(niche_keywords, target_audience, count)

        logger.debug(f"Calling LLM with prompt for {count} ideas")

        try:
            llm_output = await self.llm.generate_structured(
                prompt=prompt,
                response_schema=IdeaGenerationOutput,
                temperature=0.8,  # Higher temperature for more creative ideas
            )
        except (ValueError, TimeoutError) as e:
            logger.error(f"LLM generation failed: {e}")
            raise

        # Convert LLM output to API models
        ideas = [
            ContentIdeaPayload(
                title=idea.title,
                description=idea.description,
                status="DISCOVERED",
            )
            for idea in llm_output.ideas
        ]

        return ideas

    async def _send_webhook(
        self,
        workspace_id: str,
        ideas: List[ContentIdeaPayload],
    ) -> None:
        """
        Send generated ideas back to Spring Boot via webhook.

        Args:
            workspace_id: The workspace ID
            ideas: List of generated ideas

        Raises:
            httpx.RequestError: If the webhook request fails
        """
        webhook_url = f"{settings.backend_core_url}/api/internal/webhooks/ideas"
        payload = DiscoveryWebhookPayload(
            workspace_id=workspace_id,
            ideas=ideas,
        )

        logger.info(f"Sending webhook to {webhook_url} with {len(ideas)} ideas")

        try:
            async with httpx.AsyncClient(timeout=settings.callback_timeout) as client:
                response = await client.post(
                    webhook_url,
                    json=payload.model_dump(),
                    headers={"X-Internal-Secret": settings.internal_api_secret},
                )
                response.raise_for_status()

            logger.info(f"Webhook sent successfully. Status: {response.status_code}")

        except httpx.RequestError as e:
            logger.error(f"Webhook request failed: {e}")
            raise

    @staticmethod
    def _build_generation_prompt(
        niche_keywords: List[str],
        target_audience: str,
        count: int,
    ) -> str:
        """
        Build the prompt for idea generation.

        Args:
            niche_keywords: Niche keywords
            target_audience: Target audience description
            count: Number of ideas to generate

        Returns:
            Formatted prompt string
        """
        keywords_str = ", ".join(niche_keywords)

        prompt = f"""You are an expert content strategist and creative director.

Your task is to brainstorm {count} unique, compelling content ideas for a brand targeting this audience.

**Niche/Topic Areas:** {keywords_str}
**Target Audience:** {target_audience}

Requirements for each idea:
1. Title should be specific, actionable, and attention-grabbing
2. Description should be 2-3 sentences explaining what the content will cover
3. Reasoning should explain why this resonates with the audience

Generate {count} diverse, high-quality content ideas that:
- Are relevant to the niche keywords
- Will resonate strongly with the target audience
- Are specific enough to brief a content creator on
- Span different angles and formats (e.g., how-to, list, case study, opinion, trend analysis)

Be creative but grounded in what actually works for this audience."""

        return prompt
