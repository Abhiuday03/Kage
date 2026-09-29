package com.contentos.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * WHAT IT DOES:
 * Thread-local storage for the current authenticated user and their workspace context.
 * Provides convenient access to "who am I" and "what workspace am I in" for the current request.
 *
 * WHY IT EXISTS:
 * Instead of passing userId and workspaceId through method parameters everywhere,
 * we store them in thread-local context. This is cleaner and allows us to enforce
 * multi-tenant isolation at the repository/service level without explicit parameters.
 *
 * HOW IT CONNECTS:
 * JwtAuthenticationFilter sets this after validating the JWT token.
 * Controllers and services read it via getCurrentUserId() and getCurrentWorkspaceId().
 * TenantContextFilter validates that the user has access to the requested workspace.
 */
@Component
@Slf4j
public class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_WORKSPACE_ID = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CURRENT_USER_ID = new ThreadLocal<>();

    public void setCurrentWorkspaceId(UUID workspaceId) {
        CURRENT_WORKSPACE_ID.set(workspaceId);
    }

    public UUID getCurrentWorkspaceId() {
        UUID id = CURRENT_WORKSPACE_ID.get();
        if (id == null) {
            throw new IllegalStateException("No workspace context set for current thread");
        }
        return id;
    }

    public UUID getCurrentWorkspaceIdOrNull() {
        return CURRENT_WORKSPACE_ID.get();
    }

    public void setCurrentUserId(UUID userId) {
        CURRENT_USER_ID.set(userId);
    }

    public UUID getCurrentUserId() {
        UUID id = CURRENT_USER_ID.get();
        if (id == null) {
            throw new IllegalStateException("No user context set for current thread");
        }
        return id;
    }

    public void clear() {
        CURRENT_WORKSPACE_ID.remove();
        CURRENT_USER_ID.remove();
    }

    /**
     * Returns the currently authenticated UserPrincipal from Spring Security context.
     */
    public UserPrincipal getCurrentUserPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }
        return null;
    }
}
