package com.contentos.modules.discovery.entity;

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
 * Defines topic areas/niches that a workspace monitors for content opportunities.
 * Example: "AI in Healthcare", "SaaS Growth Marketing", "Personal Finance Tips"
 *
 * WHY IT EXISTS:
 * The AI topic discovery system needs to know what areas to focus on when scanning
 * for trends, news, and content opportunities. Each niche has keywords and target
 * audience info that guides the research and scoring algorithms.
 *
 * HOW IT CONNECTS:
 * Niche.workspace_id → Workspace
 * Niche → ContentIdeas (1:many)
 * Topic Discovery Agent uses these definitions to filter and score discovered content.
 */
@Entity
@Table(name = "niches", indexes = {
    @Index(name = "idx_niches_workspace", columnList = "workspace_id"),
    @Index(name = "idx_niches_active", columnList = "isActive")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Niche {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Keywords for discovery algorithms
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "niche_keywords", joinColumns = @JoinColumn(name = "niche_id"))
    @Column(name = "keyword")
    private List<String> keywords;

    @Column(columnDefinition = "TEXT")
    private String targetAudience;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}