package com.peercollab.backend.dto.notification;

import java.util.List;

public record NotificationSummaryResponse(
        long unreadCount,
        List<NotificationResponse> recentNotifications
) {
}
