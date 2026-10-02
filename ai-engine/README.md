# KAGE AI Engine

FastAPI microservice for content discovery and scoring in the KAGE Autonomous Content Operating System.

## Quick Start

1. **Install Dependencies**
   ```bash
   cd ai-engine
   pip install -r requirements.txt
   ```

2. **Configure Environment**
   ```bash
   cp .env.example .env
   # Edit .env with your OpenAI API key and internal secret
   ```

3. **Run the Server**
   ```bash
   python run.py
   ```

   Or with uvicorn directly:
   ```bash
   uvicorn run:app --reload --host 0.0.0.0 --port 8000
   ```

4. **Test the API**
   - Health check: `GET http://localhost:8000/`
   - API docs: `http://localhost:8000/docs`

## Architecture

```
ai-engine/
├── app/
│   ├── api/                 # FastAPI routes and dependencies
│   ├── core/               # Configuration and security
│   ├── models/             # Pydantic schemas (API & LLM)
│   ├── services/           # Business logic (Discovery Agent, Scoring Engine)
│   └── llm/                # LLM provider abstraction layer
├── requirements.txt
└── run.py                  # Entry point
```

## Key Features

- **Provider Abstraction**: Easy to swap from OpenAI to Anthropic/local models
- **Async Webhooks**: Non-blocking idea generation with callback to Spring Boot
- **Structured LLM Output**: Type-safe responses using Pydantic schemas
- **Security**: Internal API secret for service-to-service auth

## API Endpoints

### Discovery (Async)
- `POST /api/v1/discovery/generate` - Generate ideas (returns 202, processes in background)

### Scoring (Sync) 
- `POST /api/v1/scoring/evaluate` - Score an idea (returns immediately)

## Next Steps

This skeleton is ready for integration with Spring Boot. The FastAPI server can run independently and accept requests from the backend-core service.