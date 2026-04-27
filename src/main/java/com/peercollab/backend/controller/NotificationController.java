package com.peercollab.backend.controller;

import com.peercollab.backend.dto.common.PagedResponse;
import com.peercollab.backend.dto.notification.NotificationResponse;
import com.peercollab.backend.dto.notification.NotificationSummaryResponse;
import com.peercollab.backend.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public PagedResponse<NotificationResponse> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return notificationService.getCurrentUserNotifications(page, size);
    }

    @GetMapping("/summary")
    public NotificationSummaryResponse getNotificationSummary() {
        return notificationService.getNotificationSummary();
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }
}
