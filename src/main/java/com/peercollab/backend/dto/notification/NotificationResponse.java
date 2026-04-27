package com.peercollab.backend.dto.notification;

import com.peercollab.backend.entity.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        String link,
        boolean read,
        LocalDateTime createdAt
) {
}
