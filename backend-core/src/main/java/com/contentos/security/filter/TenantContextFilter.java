package com.contentos.security.filter;

import com.contentos.security.TenantContext;
import com.contentos.modules.workspace.repository.WorkspaceRepository;
import com.contentos.modules.identity.repository.OrganizationMemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * WHAT IT DOES:
 * Validates that the authenticated user has access to the requested workspace.
 * Extracts workspace ID from request path or X-Workspace-ID header.
 *
 * WHY IT EXISTS:
 * This is the CORE of multi-tenant security. It prevents users from accessing
 * data belonging to organizations they're not members of. Without this filter,
 * anyone could put any workspace ID in their request and access other people's data.
 *
 * HOW IT CONNECTS:
 * Runs AFTER JwtAuthenticationFilter (user is already authenticated).
 * Checks if the authenticated user's organizations contain the requested workspace.
 * Sets TenantContext.setCurrentWorkspaceId() if access is allowed.
 * Returns 403 Forbidden if access is denied.
 */
@Component
@Slf4j
public class TenantContextFilter extends OncePerRequestFilter {

    private final TenantContext tenantContext;
    private final WorkspaceRepository workspaceRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    public TenantContextFilter(
        TenantContext tenantContext,
        WorkspaceRepository workspaceRepository,
        OrganizationMemberRepository organizationMemberRepository
    ) {
        this.tenantContext = tenantContext;
        this.workspaceRepository = workspaceRepository;
        this.organizationMemberRepository = organizationMemberRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Skip tenant validation for non-workspace endpoints
            if (shouldSkipTenantValidation(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            UUID workspaceId = extractWorkspaceId(request);
            if (workspaceId == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Workspace ID required\"}");
                return;
            }

            // Get current authenticated user
            UUID currentUserId = tenantContext.getCurrentUserId();
            if (currentUserId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Authentication required\"}");
                return;
            }

            // Check if user has access to this workspace
            if (!hasWorkspaceAccess(currentUserId, workspaceId)) {
                log.warn("User {} attempted to access workspace {} without permission", currentUserId, workspaceId);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"Access denied to workspace\"}");
                return;
            }

            // Set workspace context for this request
            tenantContext.setCurrentWorkspaceId(workspaceId);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error in tenant context filter", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        } finally {
            // Always clear thread-local context after request
            tenantContext.clear();
        }
    }

    /**
     * Extract workspace ID from URL path or X-Workspace-ID header.
     */
    private UUID extractWorkspaceId(HttpServletRequest request) {
        // Try to extract from URL path: /api/v1/workspaces/{workspaceId}/...
        String path = request.getRequestURI();
        String[] segments = path.split("/");

        for (int i = 0; i < segments.length - 1; i++) {
            if ("workspaces".equals(segments[i]) && i + 1 < segments.length) {
                try {
                    return UUID.fromString(segments[i + 1]);
                } catch (IllegalArgumentException e) {
                    // Not a valid UUID, continue
                }
            }
        }

        // Try X-Workspace-ID header as fallback
        String headerValue = request.getHeader("X-Workspace-ID");
        if (headerValue != null) {
            try {
                return UUID.fromString(headerValue);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid workspace ID in header: {}", headerValue);
            }
        }

        return null;
    }

    /**
     * Check if user has access to workspace by verifying organization membership.
     */
    private boolean hasWorkspaceAccess(UUID userId, UUID workspaceId) {
        // Get all organization IDs that this user belongs to
        List<UUID> userOrgIds = organizationMemberRepository.findOrganizationIdsByUserId(userId);
        if (userOrgIds.isEmpty()) {
            return false;
        }

        // Check if the workspace belongs to any of the user's organizations
        return workspaceRepository.findById(workspaceId)
            .map(workspace -> userOrgIds.contains(workspace.getOrganization().getId()))
            .orElse(false);
    }

    /**
     * Skip tenant validation for endpoints that don't need workspace context.
     */
    private boolean shouldSkipTenantValidation(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/v1/auth/") ||
               path.startsWith("/api/v1/user/") ||
               path.equals("/api/v1/workspaces") || // GET workspaces list
               path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }
}