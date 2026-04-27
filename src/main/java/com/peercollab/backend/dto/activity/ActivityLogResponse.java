package com.peercollab.backend.dto.activity;

import com.peercollab.backend.entity.ActivityType;
import java.time.LocalDateTime;

public record ActivityLogResponse(
        Long id,
        String userName,
        String userEmail,
        ActivityType type,
        String description,
        String metadata,
        LocalDateTime createdAt
) {
}
