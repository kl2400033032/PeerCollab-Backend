package com.peercollab.backend.dto.project;

import com.peercollab.backend.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank(message = "Title is required.")
        @Size(min = 3, max = 140, message = "Title must be between 3 and 140 characters.")
        String title,
        @NotBlank(message = "Description is required.")
        @Size(min = 10, max = 2500, message = "Description must be between 10 and 2500 characters.")
        String description,
        @NotNull(message = "Status is required.")
        ProjectStatus status,
        Long assignmentId
) {
}
