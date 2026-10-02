# Phase 2 Integration Testing Guide

## Overview
This document outlines how to test the AI Engine integration with Spring Boot.

## Prerequisites
1. **AI Engine Running**: Start the FastAPI service on port 8000
2. **Backend Core Running**: Start the Spring Boot service on port 8080
3. **Environment Variables**: Set OpenAI API key and internal secret

## Setup Instructions

### 1. Configure AI Engine
```bash
cd ai-engine
cp .env.example .env
# Edit .env and set:
# OPENAI_API_KEY=sk-proj-your-key-here
# INTERNAL_API_SECRET=test-secret-123
# BACKEND_CORE_URL=http://localhost:8080
```

### 2. Start AI Engine
```bash
cd ai-engine
pip install -r requirements.txt
python run.py
```
Visit http://localhost:8000/docs to see the API documentation.

### 3. Configure Backend Core
Ensure your `backend-core/src/main/resources/application.yml` has:
```yaml
app:
  ai-engine:
    base-url: http://localhost:8000
    webhook-secret: test-secret-123
```

### 4. Start Backend Core
```bash
cd backend-core
./mvnw spring-boot:run
```

## API Testing Flow

### Step 1: Create a Workspace and Niche
```bash
# 1. Register/Login to get JWT token
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123","organizationName":"Test Org"}'

# 2. Login and get token
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}' | jq -r '.data.token')

# 3. Create workspace
WORKSPACE_ID=$(curl -X POST http://localhost:8080/api/v1/workspaces \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Workspace","description":"AI testing workspace"}' | jq -r '.data.id')

# 4. Create a niche
NICHE_ID=$(curl -X POST http://localhost:8080/api/v1/workspaces/$WORKSPACE_ID/niches \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Workspace-ID: $WORKSPACE_ID" \
  -H "Content-Type: application/json" \
  -d '{"name":"AI in Healthcare","description":"AI applications in medical field","keywords":["artificial intelligence","healthcare","medical AI","diagnostics"],"targetAudience":"Healthcare professionals and medical researchers"}' | jq -r '.data.id')
```

### Step 2: Trigger Discovery
```bash
# Trigger idea generation
curl -X POST "http://localhost:8080/api/v1/discovery/trigger?nicheId=$NICHE_ID&count=3" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Workspace-ID: $WORKSPACE_ID"
```

Expected response: `202 Accepted` with status "processing".

### Step 3: Check Generated Ideas
```bash
# Wait a few seconds for AI Engine to process, then check ideas
curl -X GET http://localhost:8080/api/v1/ideas \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Workspace-ID: $WORKSPACE_ID"
```

You should see generated ideas with status "DISCOVERED".

### Step 4: Score an Idea
```bash
# Get the first idea ID
IDEA_ID=$(curl -X GET http://localhost:8080/api/v1/ideas \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Workspace-ID: $WORKSPACE_ID" | jq -r '.data.content[0].id')

# Score the idea
curl -X POST "http://localhost:8080/api/v1/ideas/$IDEA_ID/score?nicheId=$NICHE_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Workspace-ID: $WORKSPACE_ID"
```

The idea should now have trend, relevance, and engagement scores.

### Step 5: Update Idea Status
```bash
# Mark idea as selected
curl -X PATCH "http://localhost:8080/api/v1/ideas/$IDEA_ID/status?status=SELECTED" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Workspace-ID: $WORKSPACE_ID"
```

## Debugging Tips

### Check AI Engine Logs
- FastAPI logs show incoming requests and LLM calls
- Look for "Calling OpenAI" messages

### Check Spring Boot Logs
- Look for "Discovery triggered" and "Ideas webhook received" messages
- Webhook failures will show error details

### Common Issues
1. **401 Unauthorized**: Check JWT token and X-Workspace-ID header
2. **403 Forbidden on webhook**: Internal secret mismatch
3. **AI Engine timeout**: Check OpenAI API key and internet connection
4. **No ideas generated**: Check AI Engine logs for LLM errors

## Expected Database State

After successful testing, you should have:
- 1 Workspace
- 1 Niche with keywords
- 3 ContentIdeas with status DISCOVERED → SCORED → SELECTED