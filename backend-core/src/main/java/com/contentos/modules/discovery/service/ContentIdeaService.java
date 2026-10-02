package com.contentos.modules.discovery.service;

import com.contentos.common.exception.ResourceNotFoundException;
import com.contentos.modules.discovery.client.AiEngineClient;
import com.contentos.modules.discovery.dto.*;
import com.contentos.modules.discovery.entity.ContentIdea;
import com.contentos.modules.discovery.entity.Niche;
import com.contentos.modules.discovery.repository.ContentIdeaRepository;
import com.contentos.modules.discovery.repository.NicheRepository;
import com.contentos.modules.workspace.entity.Workspace;
import com.contentos.modules.workspace.repository.WorkspaceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * WHAT IT DOES:
 * Manages the lifecycle of ContentIdeas: creation, scoring, status updates, retrieval.
 *
 * WHY IT EXISTS:
 * ContentIdeas go through multiple states (DISCOVERED → SCORED → SELECTED → etc.).
 * This service orchestrates those transitions and integrates with the AI Engine
 * for scoring.
 *
 * HOW IT CONNECTS:
 * - DiscoveryController (user-facing endpoints) → this service
 * - InternalWebhookController (AI Engine callbacks) → this service
 * - This service → AiEngineClient (for scoring)
 */
@Service
@Slf4j
public class ContentIdeaService {

    private final ContentIdeaRepository contentIdeaRepository;
    private final WorkspaceRepository workspaceRepository;
    private final NicheRepository nicheRepository;
    private final AiEngineClient aiEngineClient;

    public ContentIdeaService(
        ContentIdeaRepository contentIdeaRepository,
        WorkspaceRepository workspaceRepository,
        NicheRepository nicheRepository,
        AiEngineClient aiEngineClient
    ) {
        this.contentIdeaRepository = contentIdeaRepository;
        this.workspaceRepository = workspaceRepository;
        this.nicheRepository = nicheRepository;
        this.aiEngineClient = aiEngineClient;
    }

    /**
     * Get all ideas for a workspace (paginated, sorted by score).
     */
    @Transactional(readOnly = true)
    public Page<ContentIdeaDTO> getIdeas(UUID workspaceId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("totalScore").descending());
        return contentIdeaRepository.findByWorkspaceId(workspaceId, pageable)
            .map(this::toDTO);
    }

    /**
     * Get ideas filtered by status.
     */
    @Transactional(readOnly = true)
    public Page<ContentIdeaDTO> getIdeasByStatus(
        UUID workspaceId,
        ContentIdea.IdeaStatus status,
        int page,
        int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("totalScore").descending());
        return contentIdeaRepository.findByWorkspaceIdAndStatus(workspaceId, status, pageable)
            .map(this::toDTO);
    }

    /**
     * Get a single idea by ID.
     */
    @Transactional(readOnly = true)
    public ContentIdeaDTO getIdea(UUID workspaceId, UUID ideaId) {
        ContentIdea idea = contentIdeaRepository.findByIdAndWorkspaceId(ideaId, workspaceId)
            .orElseThrow(() -> new ResourceNotFoundException("ContentIdea", ideaId.toString()));
        return toDTO(idea);
    }

    /**
     * Create ideas from AI Engine webhook payload.
     *
     * This is called by InternalWebhookController when FastAPI sends
     * generated ideas back to us.
     */
    @Transactional
    public List<ContentIdeaDTO> createIdeasFromWebhook(
        UUID workspaceId,
        List<ContentIdeaPayloadDTO> ideaPayloads
    ) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new ResourceNotFoundException("Workspace", workspaceId.toString()));

        log.info("Creating {} ideas from webhook for workspace {}", ideaPayloads.size(), workspaceId);

        List<ContentIdea> ideas = ideaPayloads.stream()
            .map(payload -> ContentIdea.builder()
                .workspace(workspace)
                .title(payload.getTitle())
                .description(payload.getDescription())
                .status(ContentIdea.IdeaStatus.DISCOVERED)
                .discoveredAt(LocalDateTime.now())
                .build()
            )
            .collect(Collectors.toList());

        ideas = contentIdeaRepository.saveAll(ideas);

        log.info("Saved {} ideas to database", ideas.size());

        return ideas.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Score an idea using the AI Engine.
     *
     * This calls the FastAPI scoring endpoint and updates the ContentIdea
     * with the returned scores.
     */
    @Transactional
    public ContentIdeaDTO scoreIdea(UUID workspaceId, UUID ideaId, UUID nicheId) {
        // Fetch the idea
        ContentIdea idea = contentIdeaRepository.findByIdAndWorkspaceId(ideaId, workspaceId)
            .orElseThrow(() -> new ResourceNotFoundException("ContentIdea", ideaId.toString()));

        // Fetch the niche for context
        Niche niche = nicheRepository.findById(nicheId)
            .orElseThrow(() -> new ResourceNotFoundException("Niche", nicheId.toString()));

        // Verify niche belongs to workspace
        if (!niche.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException("Niche", nicheId.toString());
        }

        log.info("Scoring idea {} using niche {}", ideaId, niche.getName());

        // Call AI Engine
        ScoringRequestDTO request = ScoringRequestDTO.builder()
            .ideaId(ideaId.toString())
            .title(idea.getTitle())
            .description(idea.getDescription())
            .nicheKeywords(niche.getKeywords())
            .build();

        ScoringResponseDTO scoringResult = aiEngineClient.scoreIdea(request);

        // Update the idea with scores
        idea.setNiche(niche);
        idea.setTrendScore(BigDecimal.valueOf(scoringResult.getTrendScore()));
        idea.setRelevanceScore(BigDecimal.valueOf(scoringResult.getRelevanceScore()));
        idea.setEngagementScore(BigDecimal.valueOf(scoringResult.getEngagementScore()));
        idea.setTotalScore(scoringResult.getTotalScore());
        idea.setStatus(ContentIdea.IdeaStatus.SCORED);

        idea = contentIdeaRepository.save(idea);

        log.info("Scored idea {} with total score: {}", ideaId, scoringResult.getTotalScore());

        return toDTO(idea);
    }

    /**
     * Update idea status (e.g., SELECTED, REJECTED).
     */
    @Transactional
    public ContentIdeaDTO updateIdeaStatus(
        UUID workspaceId,
        UUID ideaId,
        ContentIdea.IdeaStatus newStatus
    ) {
        ContentIdea idea = contentIdeaRepository.findByIdAndWorkspaceId(ideaId, workspaceId)
            .orElseThrow(() -> new ResourceNotFoundException("ContentIdea", ideaId.toString()));

        idea.setStatus(newStatus);
        idea = contentIdeaRepository.save(idea);

        log.info("Updated idea {} status to {}", ideaId, newStatus);

        return toDTO(idea);
    }

    /**
     * Delete an idea.
     */
    @Transactional
    public void deleteIdea(UUID workspaceId, UUID ideaId) {
        ContentIdea idea = contentIdeaRepository.findByIdAndWorkspaceId(ideaId, workspaceId)
            .orElseThrow(() -> new ResourceNotFoundException("ContentIdea", ideaId.toString()));

        contentIdeaRepository.delete(idea);
        log.info("Deleted idea {} from workspace {}", ideaId, workspaceId);
    }

    private ContentIdeaDTO toDTO(ContentIdea idea) {
        return ContentIdeaDTO.builder()
            .id(idea.getId())
            .workspaceId(idea.getWorkspace().getId())
            .nicheId(idea.getNiche() != null ? idea.getNiche().getId() : null)
            .title(idea.getTitle())
            .description(idea.getDescription())
            .sourceUrl(idea.getSourceUrl())
            .trendScore(idea.getTrendScore())
            .relevanceScore(idea.getRelevanceScore())
            .engagementScore(idea.getEngagementScore())
            .totalScore(idea.getTotalScore())
            .status(idea.getStatus())
            .discoveredAt(idea.getDiscoveredAt())
            .createdAt(idea.getCreatedAt())
            .updatedAt(idea.getUpdatedAt())
            .build();
    }
}
