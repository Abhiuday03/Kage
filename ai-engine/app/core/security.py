"""
Security Utilities for AI Engine

This module provides authentication for internal service-to-service calls.

WHY: We need to ensure only our Spring Boot backend can call these endpoints.
     Using a shared secret in headers is simple and effective for internal APIs.

HOW: Every incoming request must include X-Internal-Secret header matching our config.
"""

from fastapi import HTTPException, Security, status
from fastapi.security import APIKeyHeader
from app.core.config import settings

# Define the header scheme
api_key_header = APIKeyHeader(name="X-Internal-Secret", auto_error=True)


async def verify_internal_secret(api_key: str = Security(api_key_header)) -> None:
    """
    Dependency function to verify the internal API secret.

    This is used as a FastAPI dependency in route definitions.
    If the secret doesn't match, we return 403 Forbidden.

    Args:
        api_key: The secret extracted from X-Internal-Secret header

    Raises:
        HTTPException: 403 if the secret is invalid

    Example:
        @router.post("/endpoint", dependencies=[Depends(verify_internal_secret)])
    """
    if api_key != settings.internal_api_secret:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Invalid internal API secret",
        )
