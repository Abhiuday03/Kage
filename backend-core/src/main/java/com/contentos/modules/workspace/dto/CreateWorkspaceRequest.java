package com.contentos.modules.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateWorkspaceRequest {
    @NotBlank(message = "Workspace name is required")
    @Size(min = 2, max = 255, message = "Workspace name must be between 2 and 255 characters")
    private String name;

    private String description;
}