package com.peercollab.backend.controller;

import com.peercollab.backend.dto.activity.ActivityLogResponse;
import com.peercollab.backend.dto.common.PagedResponse;
import com.peercollab.backend.service.ActivityLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final ActivityLogService activityLogService;

    public ActivityController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public PagedResponse<ActivityLogResponse> getCurrentUserActivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return activityLogService.getCurrentUserActivity(page, size);
    }
}
