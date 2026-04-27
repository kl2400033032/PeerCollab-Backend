package com.peercollab.backend.service;

import com.peercollab.backend.dto.analytics.AdminAnalyticsResponse;
import com.peercollab.backend.dto.analytics.MetricPointResponse;
import com.peercollab.backend.entity.Project;
import com.peercollab.backend.entity.ProjectStatus;
import com.peercollab.backend.entity.User;
import com.peercollab.backend.entity.UserRole;
import com.peercollab.backend.repository.ProjectRepository;
import com.peercollab.backend.repository.ReviewRepository;
import com.peercollab.backend.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ReviewRepository reviewRepository;
    private final ActivityLogService activityLogService;

    public AdminAnalyticsService(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            ReviewRepository reviewRepository,
            ActivityLogService activityLogService
    ) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.reviewRepository = reviewRepository;
        this.activityLogService = activityLogService;
    }

    @Cacheable("adminAnalytics")
    public AdminAnalyticsResponse getAnalytics() {
        List<User> students = userRepository.findByRoleOrderByNameAsc(UserRole.STUDENT);
        List<Project> projects = projectRepository.findAll();
        long totalUsers = userRepository.count();
        long totalStudents = students.size();
        long totalProjects = projects.size();
        long totalReviews = reviewRepository.count();
        double reviewCompletionRate = totalProjects == 0 ? 0.0 : Math.min(100.0, (double) totalReviews / totalProjects * 100.0);

        List<MetricPointResponse> projectsPerStudent = students.stream()
                .map(student -> new MetricPointResponse(student.getName(), projectRepository.countByStudent_Id(student.getId())))
                .toList();

        List<MetricPointResponse> statusBreakdown = Arrays.stream(ProjectStatus.values())
                .map(status -> new MetricPointResponse(status.name(), projects.stream().filter(project -> project.getStatus() == status).count()))
                .toList();

        return new AdminAnalyticsResponse(
                totalUsers,
                totalStudents,
                totalProjects,
                totalReviews,
                reviewCompletionRate,
                projectsPerStudent,
                statusBreakdown,
                activityLogService.getRecentActivity()
        );
    }
}
