package com.contentos.modules.discovery.service;

import com.contentos.common.exception.ResourceNotFoundException;
import com.contentos.modules.discovery.client.AiEngineClient;
import com.contentos.modules.discovery.dto.DiscoveryRequestDTO;
import com.contentos.modules.discovery.dto.DiscoveryResponseDTO;
import com.contentos.modules.discovery.entity.Niche;
import com.contentos.modules.discovery.repository.NicheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * WHAT IT DOES:
 * Orchestrates the topic discovery process: takes a Niche, calls the AI Engine,
 * and initiates async idea generation.
 *
 * WHY IT EXISTS:
 * Separates the "trigger discovery" logic from ContentIdeaService.
 * This service prepares the discovery request and delegates to AiEngineClient.
 *
 * HOW IT CONNECTS:
 * DiscoveryController → this service → AiEngineClient → FastAPI
 * FastAPI later calls back via InternalWebhookController → ContentIdeaService
 */
@Service
@Slf4j
public class DiscoveryService {

    private final NicheRepository nicheRepository;
    private final AiEngineClient aiEngineClient;

    public DiscoveryService(
        NicheRepository nicheRepository,
        AiEngineClient aiEngineClient
    ) {
        this.nicheRepository = nicheRepository;
        this.aiEngineClient = aiEngineClient;
    }

    /**
     * Trigger idea generation for a specific niche.
     *
     * This method:
     * 1. Fetches the niche definition
     * 2. Builds a discovery request
     * 3. Calls the AI Engine (which returns 202 Accepted)
     * 4. AI Engine processes in background and webhooks results back
     *
     * @param workspaceId The workspace
     * @param nicheId The niche to discover ideas for
     * @param count Number of ideas to generate (default 5)
     * @return DiscoveryResponseDTO with "processing" status
     */
    @Transactional(readOnly = true)
    public DiscoveryResponseDTO triggerDiscovery(UUID workspaceId, UUID nicheId, Integer count) {
        Niche niche = nicheRepository.findById(nicheId)
            .orElseThrow(() -> new ResourceNotFoundException("Niche", nicheId.toString()));

        // Verify niche belongs to workspace
        if (!niche.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException("Niche", nicheId.toString());
        }

        if (!niche.getIsActive()) {
            throw new IllegalStateException("Cannot discover ideas for inactive niche");
        }

        log.info("Triggering discovery for niche {} in workspace {}", niche.getName(), workspaceId);

        // Build discovery request
        DiscoveryRequestDTO request = DiscoveryRequestDTO.builder()
            .workspaceId(workspaceId.toString())
            .nicheKeywords(niche.getKeywords())
            .targetAudience(niche.getTargetAudience())
            .count(count != null ? count : 5)
            .build();

        // Call AI Engine (async, returns immediately)
        DiscoveryResponseDTO response = aiEngineClient.generateIdeas(request);

        log.info("Discovery initiated for workspace {}. Status: {}", workspaceId, response.getStatus());

        return response;
    }
}
