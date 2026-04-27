package com.peercollab.backend.controller;

import com.peercollab.backend.dto.analytics.AdminAnalyticsResponse;
import com.peercollab.backend.service.AdminAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    public AnalyticsController(AdminAnalyticsService adminAnalyticsService) {
        this.adminAnalyticsService = adminAnalyticsService;
    }

    @GetMapping("/admin")
    public AdminAnalyticsResponse getAdminAnalytics() {
        return adminAnalyticsService.getAnalytics();
    }
}
