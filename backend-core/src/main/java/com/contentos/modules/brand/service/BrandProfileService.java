package com.contentos.modules.brand.service;

import com.contentos.common.exception.ResourceNotFoundException;
import com.contentos.modules.brand.dto.BrandProfileDTO;
import com.contentos.modules.brand.entity.BrandProfile;
import com.contentos.modules.brand.repository.BrandProfileRepository;
import com.contentos.modules.workspace.entity.Workspace;
import com.contentos.modules.workspace.repository.WorkspaceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * WHAT IT DOES:
 * Manages brand profile CRUD for a workspace. Ensures 1:1 relationship between
 * workspace and brand profile.
 *
 * WHY IT EXISTS:
 * Brand settings are read frequently during AI generation and updated
 * through the brand settings UI.
 *
 * HOW IT CONNECTS:
 * BrandController calls this service.
 * AI agents will fetch brand profile to guide content and media generation.
 */
@Service
@Slf4j
public class BrandProfileService {

    private final BrandProfileRepository brandProfileRepository;
    private final WorkspaceRepository workspaceRepository;

    public BrandProfileService(
        BrandProfileRepository brandProfileRepository,
        WorkspaceRepository workspaceRepository
    ) {
        this.brandProfileRepository = brandProfileRepository;
        this.workspaceRepository = workspaceRepository;
    }

    /**
     * Get or create brand profile for a workspace.
     */
    @Transactional
    public BrandProfileDTO getOrCreateBrandProfile(UUID workspaceId) {
        BrandProfile profile = brandProfileRepository.findByWorkspaceId(workspaceId)
            .orElseGet(() -> createDefaultBrandProfile(workspaceId));

        return toDTO(profile);
    }

    /**
     * Update brand profile for a workspace.
     */
    @Transactional
    public BrandProfileDTO updateBrandProfile(UUID workspaceId, BrandProfileDTO dto) {
        BrandProfile profile = brandProfileRepository.findByWorkspaceId(workspaceId)
            .orElseGet(() -> {
                Workspace workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Workspace", workspaceId.toString()));
                return BrandProfile.builder().workspace(workspace).build();
            });

        // Update fields
        profile.setBrandName(dto.getBrandName());
        profile.setIndustry(dto.getIndustry());
        profile.setTargetAudience(dto.getTargetAudience());
        profile.setToneOfVoice(dto.getToneOfVoice());
        profile.setWritingStyle(dto.getWritingStyle());
        profile.setPrimaryColor(dto.getPrimaryColor());
        profile.setSecondaryColor(dto.getSecondaryColor());
        profile.setFontPrimary(dto.getFontPrimary());
        profile.setFontSecondary(dto.getFontSecondary());
        profile.setLogoUrl(dto.getLogoUrl());
        profile.setBrandGuidelines(dto.getBrandGuidelines());
        profile.setWordsToAvoid(dto.getWordsToAvoid());
        profile.setPreferredPhrases(dto.getPreferredPhrases());
        profile.setCtaPreferences(dto.getCtaPreferences());

        profile = brandProfileRepository.save(profile);
        log.info("Updated brand profile for workspace: {}", workspaceId);

        return toDTO(profile);
    }

    private BrandProfile createDefaultBrandProfile(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new ResourceNotFoundException("Workspace", workspaceId.toString()));

        BrandProfile profile = BrandProfile.builder()
            .workspace(workspace)
            .brandName(workspace.getName())
            .toneOfVoice("Professional, authoritative, and engaging")
            .writingStyle("Clear, concise, data-driven")
            .primaryColor("#4F46E5") // Indigo
            .secondaryColor("#10B981") // Emerald
            .fontPrimary("Inter")
            .fontSecondary("sans-serif")
            .build();

        return brandProfileRepository.save(profile);
    }

    private BrandProfileDTO toDTO(BrandProfile profile) {
        return BrandProfileDTO.builder()
            .id(profile.getId())
            .workspaceId(profile.getWorkspace().getId())
            .brandName(profile.getBrandName())
            .industry(profile.getIndustry())
            .targetAudience(profile.getTargetAudience())
            .toneOfVoice(profile.getToneOfVoice())
            .writingStyle(profile.getWritingStyle())
            .primaryColor(profile.getPrimaryColor())
            .secondaryColor(profile.getSecondaryColor())
            .fontPrimary(profile.getFontPrimary())
            .fontSecondary(profile.getFontSecondary())
            .logoUrl(profile.getLogoUrl())
            .brandGuidelines(profile.getBrandGuidelines())
            .wordsToAvoid(profile.getWordsToAvoid())
            .preferredPhrases(profile.getPreferredPhrases())
            .ctaPreferences(profile.getCtaPreferences())
            .build();
    }
}
