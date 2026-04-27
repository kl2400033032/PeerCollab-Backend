package com.peercollab.backend.dto.dashboard;

public record AdminDashboardResponse(
        long totalProjects,
        long totalAssignments,
        long totalSubmissions,
        long completedReviews,
        long pendingReviews
) {
}
