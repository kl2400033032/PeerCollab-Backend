package com.peercollab.backend.service;

import com.peercollab.backend.dto.dashboard.AdminDashboardResponse;
import com.peercollab.backend.dto.dashboard.StudentDashboardResponse;
import com.peercollab.backend.entity.Project;
import com.peercollab.backend.entity.User;
import com.peercollab.backend.repository.AssignmentRepository;
import com.peercollab.backend.repository.ProjectRepository;
import com.peercollab.backend.repository.ReviewRepository;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final ReviewRepository reviewRepository;
    private final AssignmentRepository assignmentRepository;
    private final CurrentUserService currentUserService;

    public DashboardService(
            ProjectRepository projectRepository,
            ReviewRepository reviewRepository,
            AssignmentRepository assignmentRepository,
            CurrentUserService currentUserService
    ) {
        this.projectRepository = projectRepository;
        this.reviewRepository = reviewRepository;
        this.assignmentRepository = assignmentRepository;
        this.currentUserService = currentUserService;
    }

    @Cacheable("adminDashboard")
    public AdminDashboardResponse getAdminDashboard() {
        List<Project> projects = projectRepository.findAll();
        long pendingReviews = projects.stream().filter(project -> project.getReviews().isEmpty()).count();
        return new AdminDashboardResponse(
                projectRepository.count(),
                assignmentRepository.count(),
                projectRepository.count(),
                reviewRepository.count(),
                pendingReviews
        );
    }

    public StudentDashboardResponse getStudentDashboard() {
        User currentUser = currentUserService.getCurrentUser();
        List<Project> myProjects = projectRepository.findByStudent_Id(currentUser.getId());
        long pendingReviews = myProjects.stream().filter(project -> project.getReviews().isEmpty()).count();
        return new StudentDashboardResponse(
                myProjects.size(),
                assignmentRepository.findByAssignedStudent_IdOrderByDueDateAsc(currentUser.getId()).size(),
                reviewRepository.countByReviewerId(currentUser.getId()),
                pendingReviews
        );
    }
}
