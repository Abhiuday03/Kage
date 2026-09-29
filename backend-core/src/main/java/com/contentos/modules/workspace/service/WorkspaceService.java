package com.contentos.modules.workspace.service;

import com.contentos.common.exception.ResourceNotFoundException;
import com.contentos.modules.workspace.dto.CreateWorkspaceRequest;
import com.contentos.modules.workspace.dto.WorkspaceDTO;
import com.contentos.modules.workspace.entity.Workspace;
import com.contentos.modules.workspace.repository.WorkspaceRepository;
import com.contentos.modules.identity.repository.OrganizationMemberRepository;
import com.contentos.security.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * WHAT IT DOES:
 * Manages workspace operations: create, list, get details.
 * Enforces tenant security (users can only see workspaces they have access to).
 *
 * WHY IT EXISTS:
 * Workspaces are the main operational boundary in the app. This service
 * provides secure access to workspace data with proper authorization checks.
 *
 * HOW IT CONNECTS:
 * WorkspaceController calls this service.
 * Uses TenantContext to get current user ID for security checks.
 */
@Service
@Slf4j
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final TenantContext tenantContext;

    public WorkspaceService(
        WorkspaceRepository workspaceRepository,
        OrganizationMemberRepository organizationMemberRepository,
        TenantContext tenantContext
    ) {
        this.workspaceRepository = workspaceRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.tenantContext = tenantContext;
    }

    /**
     * Get all workspaces accessible to the current user.
     */
    @Transactional(readOnly = true)
    public List<WorkspaceDTO> getAllWorkspaces() {
        UUID currentUserId = tenantContext.getCurrentUserId();
        List<Workspace> workspaces = workspaceRepository.findAllAccessibleByUserId(currentUserId);

        return workspaces.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get a specific workspace by ID (with security check).
     */
    @Transactional(readOnly = true)
    public WorkspaceDTO getWorkspace(UUID workspaceId) {
        UUID currentUserId = tenantContext.getCurrentUserId();

        // Security check: verify user has access to this workspace
        List<UUID> accessibleOrgIds = organizationMemberRepository.findOrganizationIdsByUserId(currentUserId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new ResourceNotFoundException("Workspace", workspaceId.toString()));

        if (!accessibleOrgIds.contains(workspace.getOrganization().getId())) {
            throw new ResourceNotFoundException("Workspace", workspaceId.toString());
        }

        return toDTO(workspace);
    }

    /**
     * Create a new workspace in the user's first organization.
     */
    @Transactional
    public WorkspaceDTO createWorkspace(CreateWorkspaceRequest request) {
        UUID currentUserId = tenantContext.getCurrentUserId();

        // Get the user's first organization
        List<UUID> orgIds = organizationMemberRepository.findOrganizationIdsByUserId(currentUserId);
        if (orgIds.isEmpty()) {
            throw new IllegalStateException("User has no organization membership");
        }

        UUID organizationId = orgIds.get(0); // Use first org for now
        String slug = generateSlug(request.getName());

        Workspace workspace = Workspace.builder()
            .organization(organizationRepository.findById(organizationId).orElseThrow())
            .name(request.getName())
            .slug(slug)
            .description(request.getDescription())
            .isActive(true)
            .build();

        workspace = workspaceRepository.save(workspace);
        log.info("Created workspace: {} for organization: {}", workspace.getName(), organizationId);

        return toDTO(workspace);
    }

    private WorkspaceDTO toDTO(Workspace workspace) {
        return WorkspaceDTO.builder()
            .id(workspace.getId())
            .name(workspace.getName())
            .slug(workspace.getSlug())
            .description(workspace.getDescription())
            .isActive(workspace.getIsActive())
            .createdAt(workspace.getCreatedAt().toEpochSecond(ZoneOffset.UTC) * 1000)
            .build();
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");
    }

    // Injected dependency for organization repository (circular dependency fix)
    @org.springframework.beans.factory.annotation.Autowired
    private com.contentos.modules.identity.repository.OrganizationRepository organizationRepository;
}