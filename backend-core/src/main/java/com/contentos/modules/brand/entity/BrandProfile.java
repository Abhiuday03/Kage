package com.contentos.modules.brand.entity;

import com.contentos.modules.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * WHAT IT DOES:
 * Stores brand guidelines and style preferences for a workspace. Influences all AI content
 * and media generation to maintain consistent brand voice, colors, and messaging.
 *
 * WHY IT EXISTS:
 * Every piece of generated content should reflect the workspace's brand identity.
 * This table stores the "DNA" that gets passed to AI agents to ensure consistent
 * tone, visual style, and messaging across all content formats.
 *
 * HOW IT CONNECTS:
 * BrandProfile.workspace_id → Workspace (1:1 relationship)
 * Referenced by AI agents when generating content briefs, images, and videos.
 * Frontend forms edit this data to customize the brand personality.
 */
@Entity
@Table(name = "brand_profiles", indexes = {
    @Index(name = "idx_brand_profiles_workspace", columnList = "workspace_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false, unique = true)
    private Workspace workspace;

    private String brandName;
    private String industry;

    @Column(columnDefinition = "TEXT")
    private String targetAudience;

    @Column(columnDefinition = "TEXT")
    private String toneOfVoice;

    @Column(columnDefinition = "TEXT")
    private String writingStyle;

    // Color palette (hex codes)
    @Column(length = 7) // #FFFFFF format
    private String primaryColor;

    @Column(length = 7)
    private String secondaryColor;

    // Typography preferences
    private String fontPrimary;
    private String fontSecondary;

    // Brand assets
    @Column(columnDefinition = "TEXT")
    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String brandGuidelines;

    // AI generation instructions
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "brand_words_to_avoid", joinColumns = @JoinColumn(name = "brand_profile_id"))
    @Column(name = "word")
    private List<String> wordsToAvoid;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "brand_preferred_phrases", joinColumns = @JoinColumn(name = "brand_profile_id"))
    @Column(name = "phrase")
    private List<String> preferredPhrases;

    @Column(columnDefinition = "TEXT")
    private String ctaPreferences;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}