"""
FastAPI Application Factory

This module creates and configures the FastAPI application.

WHY: Separating app creation from main.py allows for easier testing
     and reuse in different contexts (e.g., tests, CLI, serverless).

HOW: We configure logging, middleware, error handlers, and routes here.
"""

import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.routes import api_router
from app.core.config import settings

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Application lifespan context manager.

    Runs startup code before the app starts listening,
    and cleanup code when the app shuts down.

    WHY: This is where we initialize connections, validate config, etc.
    """
    logger.info("AI Engine starting up...")
    logger.info(f"Using LLM model: {settings.openai_model}")
    logger.info(f"Backend Core URL: {settings.backend_core_url}")

    yield

    logger.info("AI Engine shutting down...")


def create_app() -> FastAPI:
    """
    Create and configure the FastAPI application.

    Returns:
        FastAPI: Configured application instance
    """
    app = FastAPI(
        title="KAGE AI Engine",
        description="AI microservice for content discovery and scoring",
        version="0.1.0",
    )

    # Add lifespan context
    app.router.lifespan_context = lifespan

    # Add CORS middleware (allow backend-core to call us)
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["http://localhost:8080", "http://backend-core:8080"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    # Include all routes
    app.include_router(api_router)

    # Root endpoint
    @app.get("/")
    async def root():
        return {
            "service": "KAGE AI Engine",
            "version": "0.1.0",
            "status": "operational",
        }

    logger.info("FastAPI application created and configured")
    return app
