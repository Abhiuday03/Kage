-- =============================================================
-- ContentOS Database Schema — Phase 1: Multi-Tenant Foundation
-- =============================================================
-- This migration creates all tables for:
--   • User authentication
--   • Multi-tenant organization structure
--   • Workspace isolation
--   • Brand profiles
--   • Niche definitions
--
-- All data-bearing tables include workspace_id for tenant isolation.
-- =============================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================
-- USERS: Global identity table
-- One user can belong to multiple organizations
-- =============================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- =============================================================
-- ORGANIZATIONS: Billing boundary
-- Each organization can have multiple workspaces
-- =============================================================
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    billing_tier VARCHAR(50) DEFAULT 'FREE',
    stripe_customer_id VARCHAR(255),
    monthly_credits_limit INTEGER DEFAULT 100,
    monthly_credits_used INTEGER DEFAULT 0,
    credits_reset_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_organizations_slug ON organizations(slug);

-- =============================================================
-- ORGANIZATION_MEMBERS: Users belong to organizations with roles
-- =============================================================
CREATE TABLE organization_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_org_user UNIQUE(organization_id, user_id)
);

CREATE INDEX idx_org_members_org ON organization_members(organization_id);
CREATE INDEX idx_org_members_user ON organization_members(user_id);

-- =============================================================
-- WORKSPACES: Operational boundary (e.g., "Tech Blog", "Client X")
-- All content, media, and campaigns belong to a workspace
-- =============================================================
CREATE TABLE workspaces (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_org_workspace_slug UNIQUE(organization_id, slug)
);

CREATE INDEX idx_workspaces_org ON workspaces(organization_id);
CREATE INDEX idx_workspaces_slug ON workspaces(slug);

-- =============================================================
-- BRAND_PROFILES: Brand guidelines per workspace
-- Influences all content and media generation
-- =============================================================
CREATE TABLE brand_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL UNIQUE REFERENCES workspaces(id) ON DELETE CASCADE,
    brand_name VARCHAR(255),
    industry VARCHAR(255),
    target_audience TEXT,
    tone_of_voice TEXT,
    writing_style TEXT,
    primary_color VARCHAR(7),
    secondary_color VARCHAR(7),
    font_primary VARCHAR(100),
    font_secondary VARCHAR(100),
    logo_url TEXT,
    brand_guidelines TEXT,
    words_to_avoid TEXT[],
    preferred_phrases TEXT[],
    cta_preferences TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_brand_profiles_workspace ON brand_profiles(workspace_id);

-- =============================================================
-- NICHES: Topic areas / niches that a workspace monitors
-- Example: "AI in Healthcare", "SaaS Growth Marketing"
-- =============================================================
CREATE TABLE niches (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    keywords TEXT[],
    target_audience TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_niches_workspace ON niches(workspace_id);
CREATE INDEX idx_niches_active ON niches(is_active);

-- =============================================================
-- CONTENT_IDEAS: Discovered topics before they become briefs
-- Scored and prioritized for content creation
-- =============================================================
CREATE TABLE content_ideas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    niche_id UUID REFERENCES niches(id) ON DELETE SET NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    source_url TEXT,
    trend_score DECIMAL(5,2) DEFAULT 0,
    relevance_score DECIMAL(5,2) DEFAULT 0,
    engagement_score DECIMAL(5,2) DEFAULT 0,
    total_score DECIMAL(5,2) DEFAULT 0,
    status VARCHAR(50) DEFAULT 'DISCOVERED',
    discovered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_content_ideas_workspace ON content_ideas(workspace_id);
CREATE INDEX idx_content_ideas_niche ON content_ideas(niche_id);
CREATE INDEX idx_content_ideas_status ON content_ideas(status);
CREATE INDEX idx_content_ideas_score ON content_ideas(total_score DESC);

-- =============================================================
-- USAGE_RECORDS: AI/Media generation cost tracking
-- Every external API call logs here for billing
-- =============================================================
CREATE TABLE usage_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    operation_type VARCHAR(100) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    model VARCHAR(100),
    input_tokens INTEGER DEFAULT 0,
    output_tokens INTEGER DEFAULT 0,
    duration_seconds DECIMAL(10,2) DEFAULT 0,
    estimated_cost_usd DECIMAL(10,6) DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usage_records_workspace ON usage_records(workspace_id);
CREATE INDEX idx_usage_records_created ON usage_records(created_at DESC);

-- =============================================================
-- AUDIT_LOG: Security and compliance tracking
-- Every significant action is logged here
-- =============================================================
CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID REFERENCES workspaces(id) ON DELETE SET NULL,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100),
    entity_id UUID,
    metadata JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_workspace ON audit_log(workspace_id);
CREATE INDEX idx_audit_log_user ON audit_log(user_id);
CREATE INDEX idx_audit_log_created ON audit_log(created_at DESC);

-- =============================================================
-- TRIGGERS: Auto-update timestamps
-- =============================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_organizations_updated_at BEFORE UPDATE ON organizations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workspaces_updated_at BEFORE UPDATE ON workspaces
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_brand_profiles_updated_at BEFORE UPDATE ON brand_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_niches_updated_at BEFORE UPDATE ON niches
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_content_ideas_updated_at BEFORE UPDATE ON content_ideas
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
