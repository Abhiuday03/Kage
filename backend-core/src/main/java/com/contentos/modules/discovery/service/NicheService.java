package com.contentos.modules.discovery.service;

import com.contentos.common.exception.ResourceNotFoundException;
import com.contentos.modules.discovery.dto.NicheDTO;
import com.contentos.modules.discovery.entity.Niche;
import com.contentos.modules.discovery.repository.NicheRepository;
import com.contentos.modules.workspace.entity.Workspace;
import com.contentos.modules.workspace.repository.WorkspaceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * WHAT IT DOES:
 * Manages niche definitions for a workspace (topic areas monitored for content).
 *
 * WHY IT EXISTS:
 * Users define their niches with keywords to guide AI discovery algorithms.
 *
 * HOW IT CONNECTS:
 * NicheController calls this service.
 * Topic Discovery Agent (Phase 2) reads these niches to know what to search for.
 */
@Service
@Slf4j
public class NicheService {

    private final NicheRepository nicheRepository;
    private final WorkspaceRepository workspaceRepository;

    public NicheService(
        NicheRepository nicheRepository,
        WorkspaceRepository workspaceRepository
    ) {
        this.nicheRepository = nicheRepository;
        this.workspaceRepository = workspaceRepository;
    }

    /**
     * Get all niches for a workspace.
     */
    @Transactional(readOnly = true)
    public List<NicheDTO> getNiches(UUID workspaceId) {
        return nicheRepository.findByWorkspaceId(workspaceId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Create a new niche in a workspace.
     */
    @Transactional
    public NicheDTO createNiche(UUID workspaceId, NicheDTO dto) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new ResourceNotFoundException("Workspace", workspaceId.toString()));

        Niche niche = Niche.builder()
            .workspace(workspace)
            .name(dto.getName())
            .description(dto.getDescription())
            .keywords(dto.getKeywords())
            .targetAudience(dto.getTargetAudience())
            .isActive(true)
            .build();

        niche = nicheRepository.save(niche);
        log.info("Created niche: {} in workspace: {}", niche.getName(), workspaceId);

        return toDTO(niche);
    }

    /**
     * Update an existing niche.
     */
    @Transactional
    public NicheDTO updateNiche(UUID workspaceId, UUID nicheId, NicheDTO dto) {
        Niche niche = nicheRepository.findById(nicheId)
            .orElseThrow(() -> new ResourceNotFoundException("Niche", nicheId.toString()));

        // Security check: verify niche belongs to workspace
        if (!niche.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException("Niche", nicheId.toString());
        }

        niche.setName(dto.getName());
        niche.setDescription(dto.getDescription());
        niche.setKeywords(dto.getKeywords());
        niche.setTargetAudience(dto.getTargetAudience());
        if (dto.getIsActive() != null) {
            niche.setIsActive(dto.getIsActive());
        }

        niche = nicheRepository.save(niche);
        log.info("Updated niche: {} in workspace: {}", niche.getName(), workspaceId);

        return toDTO(niche);
    }

    /**
     * Delete a niche.
     */
    @Transactional
    public void deleteNiche(UUID workspaceId, UUID nicheId) {
        Niche niche = nicheRepository.findById(nicheId)
            .orElseThrow(() -> new ResourceNotFoundException("Niche", nicheId.toString()));

        if (!niche.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException("Niche", nicheId.toString());
        }

        nicheRepository.delete(niche);
        log.info("Deleted niche: {} from workspace: {}", nicheId, workspaceId);
    }

    private NicheDTO toDTO(Niche niche) {
        return NicheDTO.builder()
            .id(niche.getId())
            .workspaceId(niche.getWorkspace().getId())
            .name(niche.getName())
            .description(niche.getDescription())
            .keywords(niche.getKeywords())
            .targetAudience(niche.getTargetAudience())
            .isActive(niche.getIsActive())
            .build();
    }
}
