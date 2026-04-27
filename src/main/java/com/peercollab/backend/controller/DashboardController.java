package com.peercollab.backend.controller;

import com.peercollab.backend.dto.dashboard.AdminDashboardResponse;
import com.peercollab.backend.dto.dashboard.StudentDashboardResponse;
import com.peercollab.backend.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    public AdminDashboardResponse getAdminDashboard() {
        return dashboardService.getAdminDashboard();
    }

    @GetMapping("/student")
    public StudentDashboardResponse getStudentDashboard() {
        return dashboardService.getStudentDashboard();
    }
}
