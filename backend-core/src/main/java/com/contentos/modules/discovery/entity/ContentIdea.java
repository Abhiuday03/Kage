package com.contentos.modules.discovery.entity;

import com.contentos.modules.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WHAT IT DOES:
 * Stores a discovered content idea (topic opportunity) before it becomes a formal brief.
 * Includes scoring dimensions for prioritization.
 *
 * WHY IT EXISTS:
 * The system discovers many potential topics daily. Each idea needs to be scored on
 * relevance, trending-ness, and engagement potential before a human selects it.
 * Status tracks the idea through its lifecycle: DISCOVERED → SELECTED → BRIEFED → etc.
 *
 * HOW IT CONNECTS:
 * ContentIdea.workspace_id → Workspace
 * ContentIdea.niche_id → Niche
 * ContentIdea → ContentBrief (Phase 2)
 * Appears in the "Ideas" section of the frontend dashboard.
 */
@Entity
@Table(name = "content_ideas", indexes = {
    @Index(name = "idx_content_ideas_workspace", columnList = "workspace_id"),
    @Index(name = "idx_content_ideas_niche", columnList = "niche_id"),
    @Index(name = "idx_content_ideas_status", columnList = "status"),
    @Index(name = "idx_content_ideas_score", columnList = "totalScore")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentIdea {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "niche_id")
    private Niche niche;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String sourceUrl;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal trendScore = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal relevanceScore = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal engagementScore = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal totalScore = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private IdeaStatus status = IdeaStatus.DISCOVERED;

    private LocalDateTime discoveredAt;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum IdeaStatus {
        DISCOVERED,   // Just found by the discovery agent
        SCORED,       // Scoring has been computed
        SELECTED,     // Human selected it for content creation
        BRIEFED,      // Content brief has been created
        IN_PROGRESS,  // Content generation underway
        COMPLETED,    // All content generated and published
        REJECTED,     // Human rejected this idea
        ARCHIVED      // No longer active
    }
}