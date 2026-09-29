# KAGE — AI-Powered Content Operating System

> **Status: Phase 1 Complete** ✓
> 
> Multi-tenant foundations, authentication, workspace management, and brand/niche configuration.

---

## 🎯 What is KAGE?

KAGE is a production-grade SaaS platform that automates the entire content creation lifecycle:

1. **Discover** emerging topics in your niche
2. **Research** credible sources and synthesize insights
3. **Brief** create structured content outlines
4. **Generate** multi-platform content variants (LinkedIn, X, Instagram, etc.)
5. **Visualize** create branded images and media assets
6. **Video** produce vertical reels and short-form videos
7. **Approve** human-in-the-loop review workflows
8. **Publish** distribute to multiple platforms
9. **Analyze** track performance and optimize
10. **Learn** feed insights back into future content

---

## 🏗️ Architecture

### Tech Stack

**Frontend:** React 18 + Vite + TypeScript + TanStack Query + Tailwind CSS
**Backend:** Spring Boot 3 + Java 21 + PostgreSQL + Redis
**AI Engine:** FastAPI + Python + LangGraph (Phase 2)
**Storage:** AWS S3 / Cloudflare R2 / MinIO (S3-compatible)
**Deployment:** Docker (local) → AWS / Cloudflare (production)

### System Design

```
┌─────────────────────────────────────────────────────────────────────┐
│                      React SPA (Frontend)                           │
│                   Port 5173 (Development)                           │
└────────────────────────────┬────────────────────────────────────────┘
                             │ REST / TanStack Query
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   Spring Boot 3 Core API                            │
│        Port 8080 (Multi-tenant, JWT Auth, RBAC)                   │
│                                                                     │
│  ✓ Authentication (JWT tokens, stateless)                         │
│  ✓ Tenant Isolation (workspace_id scoping)                        │
│  ✓ Organization & Workspace Management                             │
│  ✓ Brand Profile & Niche Configuration                             │
│  ✓ Content Ideas & Research (Phase 2)                              │
└────────────────────────────┬────────────────────────────────────────┘
             │               │               │
             ▼               ▼               ▼
    ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
    │  PostgreSQL  │ │    Redis     │ │  MinIO S3    │
    │   (Primary)  │ │  (Cache/Q)   │ │ (Media Stor) │
    │  Port 5432   │ │  Port 6379   │ │  Port 9000   │
    └──────────────┘ └──────────────┘ └──────────────┘
```

---

## 🚀 Getting Started

### Prerequisites

- **Docker Desktop** (includes docker-compose)
- **Java 21** (for Spring Boot development)
- **Node.js 18+** (for React development)
- **Maven 3.8+** (for building backend)

### Step 1: Setup Infrastructure

```bash
# Start PostgreSQL, Redis, MinIO in Docker
make docker-up

# Verify all services are healthy
make health-check
```

Expected output:
```
✓ PostgreSQL healthy
✓ Redis healthy
✓ MinIO healthy
```

### Step 2: Configure Environment

```bash
# Copy example environment variables
make env-copy

# Edit .env with your API keys
# (Skip for now — we'll use mock values locally)
```

### Step 3: Build Backend

```bash
# Install dependencies and compile
make backend-build

# Run database migrations (automatic via Spring Boot)
make backend-run
```

The API will be available at `http://localhost:8080`

**Swagger UI documentation:** http://localhost:8080/swagger-ui.html

### Step 4: Build & Run Frontend

```bash
# Install React dependencies
make frontend-install

# Start dev server
make frontend-dev
```

The frontend will be available at `http://localhost:5173`

### Step 5: Test the Full Stack

#### Register a new account

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john@example.com",
    "password": "SecurePassword123",
    "organizationName": "My Company"
  }'
```

Expected response:
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "john@example.com",
      "fullName": "John Doe"
    },
    "organization": {
      "id": "660e8400-e29b-41d4-a716-446655440000",
      "name": "My Company",
      "slug": "my-company"
    }
  },
  "timestamp": 1696000000000
}
```

#### Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePassword123"
  }'
```

#### Get workspaces

```bash
TOKEN="<token_from_register>"

curl -X GET http://localhost:8080/api/v1/workspaces \
  -H "Authorization: Bearer $TOKEN"
```

#### Create a niche

```bash
WORKSPACE_ID="<workspace_id>"

curl -X POST http://localhost:8080/api/v1/workspaces/$WORKSPACE_ID/niches \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "AI in Healthcare",
    "description": "Emerging AI applications in medical technology",
    "keywords": ["AI", "healthcare", "medical", "diagnosis"],
    "targetAudience": "Healthcare professionals and CIOs"
  }'
```

---

## 📁 Project Structure

```
kage/
├── docker-compose.yml          # Local infrastructure (PostgreSQL, Redis, MinIO)
├── .env.example                # Environment variables template
├── Makefile                    # Development commands
├── README.md                   # This file
│
├── frontend/                   # React SPA
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── src/
│       ├── app/               # Main App component & routing
│       ├── components/        # Reusable UI components
│       ├── features/          # Feature-specific code
│       ├── hooks/             # Custom React hooks
│       ├── services/          # API client
│       ├── types/             # TypeScript interfaces
│       └── utils/             # Utilities & helpers
│
├── backend-core/              # Spring Boot 3 API
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/com/KAGE/
│   │   │   ├── Application.java
│   │   │   ├── common/        # Shared exceptions, responses
│   │   │   ├── config/        # Security, database config
│   │   │   ├── security/      # JWT, tenant context, filters
│   │   │   └── modules/       # Domain modules
│   │   │       ├── identity/  # Auth, users, orgs
│   │   │       ├── workspace/ # Workspace CRUD
│   │   │       ├── brand/     # Brand profiles
│   │   │       └── discovery/ # Topics, ideas
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/  # Flyway SQL scripts
│   └── src/test/             # JUnit tests
│
└── ai-engine/                 # FastAPI (Python) — Phase 2
    ├── pyproject.toml
    ├── Dockerfile
    └── app/
        ├── main.py
        ├── agents/            # LLM agents
        ├── providers/         # AI provider abstractions
        └── workers/           # Background job workers
```

---

## 🔐 Security & Multi-Tenancy

### How Multi-Tenant Isolation Works

1. **Authentication:** Users log in with email/password, receive JWT token
2. **Tenant Context:** Every request includes workspace ID in URL path or header
3. **Validation:** `TenantContextFilter` verifies user belongs to that organization
4. **Data Isolation:** All queries automatically filtered by `workspace_id`

Example flow:
```
GET /api/v1/workspaces/{workspaceId}/niches
  ↓
TenantContextFilter: "Does this user have access to {workspaceId}?"
  ├─ No: Return 403 Forbidden
  └─ Yes: Continue
  ↓
NicheController: Set TenantContext.setCurrentWorkspaceId()
  ↓
NicheService: Query niches for THIS workspace only
  ↓
Database: SELECT * FROM niches WHERE workspace_id = ?
```

### Password Security

- Passwords are hashed with **BCrypt** (never stored in plain text)
- JWT tokens expire after 7 days (configurable)
- All endpoints require JWT except `/auth/register` and `/auth/login`

---

## 📊 Database Schema (Phase 1)

```
users
├── id (UUID primary key)
├── email (unique)
├── password_hash
├── full_name
└── created_at

organizations
├── id (UUID)
├── name
├── slug (unique)
├── billing_tier
├── stripe_customer_id
├── monthly_credits_limit
└── monthly_credits_used

organization_members
├── id (UUID)
├── organization_id (FK)
├── user_id (FK)
├── role (OWNER, ADMIN, MEMBER, VIEWER)
└── joined_at

workspaces
├── id (UUID)
├── organization_id (FK)
├── name
├── slug
└── description

brand_profiles
├── id (UUID)
├── workspace_id (FK, unique)
├── brand_name
├── tone_of_voice
├── writing_style
├── primary_color
├── secondary_color
└── ... (more fields)

niches
├── id (UUID)
├── workspace_id (FK)
├── name
├── keywords (TEXT[])
└── target_audience

content_ideas
├── id (UUID)
├── workspace_id (FK)
├── niche_id (FK)
├── title
├── trend_score
├── relevance_score
├── engagement_score
├── total_score
└── status
```

---

## 🧪 Testing

### Run Backend Tests

```bash
make backend-test
```

### Run Frontend Tests

```bash
make frontend-test
```

---

## 🐛 Troubleshooting

### "Connection refused" for PostgreSQL

```bash
# Check if containers are running
docker compose ps

# Restart containers
make docker-down && make docker-up

# Wait 10 seconds for databases to initialize
```

### "Port 8080 already in use"

```bash
# Find what's using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### "No database tables found"

The backend automatically runs Flyway migrations on startup. Check logs:

```bash
make backend-run
# Look for: "Flyway successfully validated X migrations"
```

### Reset everything to clean state

```bash
make docker-clean
make docker-up
make backend-run
```

---

## 📖 API Documentation

Swagger UI is available at: **http://localhost:8080/swagger-ui.html**

All endpoints return a standardized response format:

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": 1696000000000
}
```

### Key Endpoints (Phase 1)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Create new account |
| POST | `/api/v1/auth/login` | Authenticate & get JWT |
| GET | `/api/v1/workspaces` | List user's workspaces |
| POST | `/api/v1/workspaces` | Create new workspace |
| GET | `/api/v1/workspaces/{id}` | Get workspace details |
| GET | `/api/v1/workspaces/{id}/brand` | Get brand profile |
| PUT | `/api/v1/workspaces/{id}/brand` | Update brand profile |
| GET | `/api/v1/workspaces/{id}/niches` | List niches |
| POST | `/api/v1/workspaces/{id}/niches` | Create niche |

---

## 🚦 Next Steps (Phase 2)

Phase 1 ✓ Complete:
- ✓ Authentication & multi-tenancy
- ✓ Workspace management
- ✓ Brand & niche configuration
- ✓ REST API foundation

Phase 2 (Coming Soon):
- [ ] AI Topic Discovery Engine
- [ ] Research & Source Synthesis
- [ ] Content Brief Generation
- [ ] Multi-format Content Variants
- [ ] Image Generation Pipeline

---

## 🤝 Contributing

This is your personal project codebase. Make commits with meaningful messages:

```bash
git add .
git commit -m "feat: add topic discovery agent"
git push origin feature/topic-discovery
```

---

## 📝 License

Private — KAGE (2026)

---

## 🆘 Support

For issues or questions about the architecture, check:
1. Swagger UI docs
2. Code comments (each file explains its purpose)
3. This README

---

**Happy building! 🚀**
