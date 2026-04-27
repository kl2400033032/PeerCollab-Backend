package com.peercollab.backend.dto.dashboard;

public record StudentDashboardResponse(
        long totalProjects,
        long assignedProjects,
        long completedReviews,
        long pendingReviews
) {
}
