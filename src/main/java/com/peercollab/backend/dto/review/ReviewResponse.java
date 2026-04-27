package com.peercollab.backend.dto.review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        String reviewerName,
        String reviewerEmail,
        String feedback,
        Integer rating,
        LocalDateTime createdAt
) {
}
