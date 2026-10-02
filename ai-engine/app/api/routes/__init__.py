"""
API Routes Module Initialization

This file registers all route modules with the main FastAPI application.
"""

from fastapi import APIRouter

from app.api.routes import discovery, scoring

# Create a main router that includes all sub-routers
api_router = APIRouter()
api_router.include_router(discovery.router)
api_router.include_router(scoring.router)

__all__ = ["api_router"]
