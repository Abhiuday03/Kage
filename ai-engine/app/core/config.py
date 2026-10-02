"""
Configuration Management for AI Engine

This module loads all environment variables and validates them at startup.
Using Pydantic Settings ensures we fail fast if required configs are missing.

WHY: Centralized config management prevents scattered os.getenv() calls
     and makes it easy to see all dependencies at a glance.
"""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """
    Application Settings

    All values are loaded from environment variables or .env file.
    Pydantic will raise a ValidationError if required fields are missing.
    """

    # Internal Security
    internal_api_secret: str  # Shared secret for inter-service communication

    # OpenAI Configuration
    openai_api_key: str
    openai_model: str = "gpt-4o"  # Default to GPT-4o for reasoning tasks

    # Backend Integration
    backend_core_url: str  # Base URL for Spring Boot callbacks

    # Server Configuration
    host: str = "0.0.0.0"
    port: int = 8000

    # Request Timeout Settings (in seconds)
    llm_timeout: int = 60
    callback_timeout: int = 10

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
    )


# Global settings instance
# This will be initialized once at startup and imported throughout the app
settings = Settings()
