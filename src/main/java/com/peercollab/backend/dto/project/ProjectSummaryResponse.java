package com.peercollab.backend.dto.project;

import com.peercollab.backend.entity.ProjectStatus;
import java.time.LocalDateTime;

public record ProjectSummaryResponse(
        Long id,
        String title,
        String description,
        String studentName,
        String studentEmail,
        ProjectStatus status,
        int reviewCount,
        int commentCount,
        Double averageRating,
        Long assignmentId,
        String assignmentTitle,
        ProjectFileResponse attachment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
