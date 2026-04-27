package com.peercollab.backend.dto.comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String authorName,
        String authorEmail,
        String message,
        LocalDateTime createdAt
) {
}
