package com.peercollab.backend.dto.project;

import com.peercollab.backend.dto.comment.CommentResponse;
import com.peercollab.backend.dto.review.ReviewResponse;
import com.peercollab.backend.entity.ProjectStatus;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectDetailsResponse(
        Long id,
        String title,
        String description,
        String studentName,
        String studentEmail,
        ProjectStatus status,
        Long assignmentId,
        String assignmentTitle,
        ProjectFileResponse attachment,
        List<ReviewResponse> reviews,
        List<CommentResponse> comments,
        int reviewCount,
        int commentCount,
        Double averageRating,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
