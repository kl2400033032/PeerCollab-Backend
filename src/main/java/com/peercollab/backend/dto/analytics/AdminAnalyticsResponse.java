package com.peercollab.backend.dto.analytics;

import com.peercollab.backend.dto.activity.ActivityLogResponse;
import java.util.List;

public record AdminAnalyticsResponse(
        long totalUsers,
        long totalStudents,
        long totalProjects,
        long totalReviews,
        double reviewCompletionRate,
        List<MetricPointResponse> projectsPerStudent,
        List<MetricPointResponse> projectStatusBreakdown,
        List<ActivityLogResponse> recentActivities
) {
}
