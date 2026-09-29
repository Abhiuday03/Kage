package com.contentos.modules.discovery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NicheDTO {
    private UUID id;
    private UUID workspaceId;

    @NotBlank(message = "Niche name is required")
    private String name;

    private String description;
    private List<String> keywords;
    private String targetAudience;
    private Boolean isActive;
}
