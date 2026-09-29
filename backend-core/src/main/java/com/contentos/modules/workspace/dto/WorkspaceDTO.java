package com.contentos.modules.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceDTO {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private Boolean isActive;
    private Long createdAt;
}
