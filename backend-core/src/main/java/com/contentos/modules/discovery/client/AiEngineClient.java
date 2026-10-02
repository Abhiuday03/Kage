package com.contentos.modules.discovery.client;

import com.contentos.modules.discovery.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * WHAT IT DOES:
 * HTTP client for communicating with the FastAPI AI Engine microservice.
 * Handles all REST API calls to the Python service.
 *
 * WHY IT EXISTS:
 * Centralizes AI Engine communication logic and handles authentication
 * (internal webhook secret), error handling, and retries.
 *
 * HOW IT CONNECTS:
 * ContentIdeaService and DiscoveryService call this client.
 * This client makes HTTP requests to FastAPI endpoints.
 */
@Component
@Slf4j
public class AiEngineClient {

    private final RestTemplate restTemplate;
    private final String aiEngineBaseUrl;
    private final String webhookSecret;

    public AiEngineClient(
        RestTemplate restTemplate,
        @Value("${app.ai-engine.base-url}") String aiEngineBaseUrl,
        @Value("${app.ai-engine.webhook-secret}") String webhookSecret
    ) {
        this.restTemplate = restTemplate;
        this.aiEngineBaseUrl = aiEngineBaseUrl;
        this.webhookSecret = webhookSecret;
    }

    /**
     * Trigger async idea generation in the AI Engine.
     *
     * This endpoint returns 202 Accepted immediately.
     * Results arrive later via webhook to /api/internal/webhooks/ideas.
     *
     * @param request Discovery parameters (workspace, niche, audience)
     * @return DiscoveryResponseDTO with "processing" status
     * @throws RestClientException if AI Engine is unreachable or returns an error
     */
    public DiscoveryResponseDTO generateIdeas(DiscoveryRequestDTO request) {
        String url = aiEngineBaseUrl + "/api/v1/discovery/generate";

        log.info("Calling AI Engine to generate ideas for workspace: {}", request.getWorkspaceId());

        try {
            HttpHeaders headers = createAuthenticatedHeaders();
            HttpEntity<DiscoveryRequestDTO> entity = new HttpEntity<>(request, headers);

            ResponseEntity<DiscoveryResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                DiscoveryResponseDTO.class
            );

            log.info("AI Engine accepted discovery request: {}", response.getBody());
            return response.getBody();

        } catch (RestClientException e) {
            log.error("Failed to call AI Engine for idea generation: {}", e.getMessage());
            throw new RuntimeException("AI Engine unavailable. Please try again later.", e);
        }
    }

    /**
     * Score a single content idea (synchronous).
     *
     * Returns scores for trend, relevance, and engagement immediately.
     *
     * @param request Idea to score with niche context
     * @return ScoringResponseDTO with individual and total scores
     * @throws RestClientException if AI Engine is unreachable or returns an error
     */
    public ScoringResponseDTO scoreIdea(ScoringRequestDTO request) {
        String url = aiEngineBaseUrl + "/api/v1/scoring/evaluate";

        log.info("Calling AI Engine to score idea: {}", request.getIdeaId());

        try {
            HttpHeaders headers = createAuthenticatedHeaders();
            HttpEntity<ScoringRequestDTO> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ScoringResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                ScoringResponseDTO.class
            );

            log.info("AI Engine scored idea {} with total score: {}",
                request.getIdeaId(),
                response.getBody().getTotalScore()
            );

            return response.getBody();

        } catch (RestClientException e) {
            log.error("Failed to call AI Engine for scoring: {}", e.getMessage());
            throw new RuntimeException("AI Engine unavailable. Please try again later.", e);
        }
    }

    /**
     * Create HTTP headers with internal authentication secret.
     *
     * The AI Engine validates this secret on every request.
     */
    private HttpHeaders createAuthenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Secret", webhookSecret);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
