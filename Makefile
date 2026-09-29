# =============================================================
# ContentOS — Development Makefile
# =============================================================
# Common tasks for local development.
# Run: make help

.PHONY: help docker-up docker-down build backend frontend test clean logs

help:
	@echo "ContentOS Development Commands"
	@echo "=============================="
	@echo ""
	@echo "Infrastructure:"
	@echo "  make docker-up          Start PostgreSQL, Redis, MinIO locally"
	@echo "  make docker-down        Stop all containers"
	@echo "  make docker-logs        View container logs"
	@echo ""
	@echo "Backend (Spring Boot):"
	@echo "  make backend-build      Compile backend (Maven)"
	@echo "  make backend-run        Start backend API server (port 8080)"
	@echo "  make backend-test       Run backend tests"
	@echo ""
	@echo "Frontend (React):"
	@echo "  make frontend-install   Install Node dependencies"
	@echo "  make frontend-dev       Start dev server (port 5173)"
	@echo ""
	@echo "Database:"
	@echo "  make db-migrate         Run Flyway migrations"
	@echo ""
	@echo "Cleanup:"
	@echo "  make clean              Remove all build artifacts"
	@echo "  make docker-clean       Delete all volumes (reset to clean state)"

# =============================================================
# Docker Infrastructure
# =============================================================

docker-up:
	@echo "Starting Docker containers..."
	docker compose up -d
	@echo "✓ PostgreSQL on localhost:5432"
	@echo "✓ Redis on localhost:6379"
	@echo "✓ MinIO API on localhost:9000"
	@echo "✓ MinIO Console on localhost:9001 (minioadmin / minioadmin)"
	@echo ""
	@echo "Waiting for services to be healthy..."
	@sleep 5
	docker compose ps

docker-down:
	@echo "Stopping Docker containers..."
	docker compose down

docker-clean:
	@echo "WARNING: This will delete all Docker volumes (clean slate)"
	@read -p "Continue? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		docker compose down -v; \
		echo "✓ All volumes deleted"; \
	fi

docker-logs:
	docker compose logs -f

# =============================================================
# Backend (Spring Boot / Maven)
# =============================================================

backend-build:
	@echo "Building backend..."
	cd backend-core && mvn clean package -DskipTests
	@echo "✓ Backend built (target/backend-core-*.jar)"

backend-run:
	@echo "Starting backend API server (port 8080)..."
	cd backend-core && mvn spring-boot:run

backend-test:
	@echo "Running backend tests..."
	cd backend-core && mvn test

backend-install-deps:
	@echo "Installing backend dependencies..."
	cd backend-core && mvn dependency:resolve

# =============================================================
# Frontend (React / Node)
# =============================================================

frontend-install:
	@echo "Installing frontend dependencies..."
	cd frontend && npm install
	@echo "✓ Dependencies installed"

frontend-dev:
	@echo "Starting frontend dev server (port 5173)..."
	cd frontend && npm run dev

frontend-build:
	@echo "Building frontend for production..."
	cd frontend && npm run build

frontend-test:
	@echo "Running frontend tests..."
	cd frontend && npm test

# =============================================================
# Database
# =============================================================

db-migrate:
	@echo "Running Flyway migrations..."
	cd backend-core && mvn flyway:migrate -Dflyway.configFiles=src/main/resources/flyway.conf

db-info:
	@echo "Database migration status:"
	cd backend-core && mvn flyway:info

# =============================================================
# Development Workflow
# =============================================================

setup: docker-up backend-install-deps frontend-install
	@echo "✓ Full development environment ready"
	@echo ""
	@echo "Next steps:"
	@echo "1. Run the backend:  make backend-run"
	@echo "2. In another terminal, run the frontend:  make frontend-dev"
	@echo "3. Open http://localhost:5173 in your browser"

run-all:
	@echo "Starting all services (runs in background)..."
	docker compose up -d
	@echo "✓ Docker containers started"
	@echo ""
	@echo "Start backend in one terminal:    make backend-run"
	@echo "Start frontend in another:        make frontend-dev"

clean:
	@echo "Cleaning build artifacts..."
	cd backend-core && mvn clean
	cd frontend && rm -rf node_modules dist
	@echo "✓ Cleaned"

# =============================================================
# Utility
# =============================================================

env-copy:
	@echo "Copying .env.example to .env..."
	cp .env.example .env
	@echo "⚠ Edit .env with your API keys before running the app"

logs-backend:
	docker compose logs -f postgres redis minio

health-check:
	@echo "Health checks:"
	@echo ""
	@echo "PostgreSQL:"
	@docker exec contentos_postgres pg_isready -U contentos_user || echo "❌ PostgreSQL not running"
	@echo ""
	@echo "Redis:"
	@docker exec contentos_redis redis-cli -a contentos_redis_pass ping || echo "❌ Redis not running"
	@echo ""
	@echo "MinIO:"
	@curl -s http://localhost:9000/minio/health/live > /dev/null && echo "✓ MinIO healthy" || echo "❌ MinIO not running"
	@echo ""
	@echo "Backend API:"
	@curl -s http://localhost:8080/actuator/health > /dev/null && echo "✓ Backend healthy" || echo "❌ Backend not running"
	@echo ""
	@echo "Frontend:"
	@curl -s http://localhost:5173 > /dev/null && echo "✓ Frontend healthy" || echo "❌ Frontend not running"
