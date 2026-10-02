"""
AI Engine Entry Point

This script starts the FastAPI server.

To run:
    uvicorn run:app --reload --host 0.0.0.0 --port 8000

Environment:
    - Copy .env.example to .env
    - Set OPENAI_API_KEY and INTERNAL_API_SECRET
"""

from app import create_app

app = create_app()

if __name__ == "__main__":
    import uvicorn
    from app.core.config import settings

    uvicorn.run(
        "run:app",
        host=settings.host,
        port=settings.port,
        reload=True,
    )
