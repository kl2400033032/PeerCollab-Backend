package com.peercollab.backend.service;

import com.peercollab.backend.dto.review.ReviewRequest;
import com.peercollab.backend.dto.review.ReviewResponse;
import com.peercollab.backend.entity.ActivityType;
import com.peercollab.backend.entity.NotificationType;
import com.peercollab.backend.entity.Project;
import com.peercollab.backend.entity.ProjectStatus;
import com.peercollab.backend.entity.Review;
import com.peercollab.backend.entity.User;
import com.peercollab.backend.entity.UserRole;
import com.peercollab.backend.exception.BadRequestException;
import com.peercollab.backend.exception.ForbiddenException;
import com.peercollab.backend.exception.ResourceNotFoundException;
import com.peercollab.backend.repository.ProjectRepository;
import com.peercollab.backend.repository.ReviewRepository;
import java.time.LocalDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;
    private final EmailService emailService;

    public ReviewService(
            ReviewRepository reviewRepository,
            ProjectRepository projectRepository,
            CurrentUserService currentUserService,
            NotificationService notificationService,
            ActivityLogService activityLogService,
            EmailService emailService
    ) {
        this.reviewRepository = reviewRepository;
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
        this.emailService = emailService;
    }

    @CacheEvict(value = {"projects", "projectDetails", "studentProjects", "adminDashboard", "studentDashboard", "adminAnalytics", "notifications"}, allEntries = true)
    public ReviewResponse addReview(Long projectId, ReviewRequest request) {
        User reviewer = currentUserService.getCurrentUser();
        if (reviewer.getRole() != UserRole.STUDENT) {
            throw new ForbiddenException("Only students can submit reviews.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (project.getStudent().getId().equals(reviewer.getId())) {
            throw new BadRequestException("Students cannot review their own projects.");
        }

        if (reviewRepository.existsByProjectIdAndReviewerId(projectId, reviewer.getId())) {
            throw new BadRequestException("You have already reviewed this project.");
        }

        Review review = new Review();
        review.setProject(project);
        review.setReviewer(reviewer);
        review.setReviewerName(reviewer.getName());
        review.setFeedback(request.feedback().trim());
        review.setRating(request.rating());
        review.setCreatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);

        if (project.getStatus() == ProjectStatus.SUBMITTED) {
            project.setStatus(ProjectStatus.UNDER_REVIEW);
        }
        if (reviewRepository.findByProjectIdOrderByCreatedAtDesc(projectId).size() >= 2) {
            project.setStatus(ProjectStatus.COMPLETED);
        }
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);

        activityLogService.log(reviewer, ActivityType.REVIEW_ADDED, "Reviewed project " + project.getTitle(), "projectId=" + project.getId());
        notificationService.createNotification(
                project.getStudent(),
                NotificationType.REVIEW_RECEIVED,
                "New review received",
                reviewer.getName() + " reviewed your project \"" + project.getTitle() + "\".",
                "/projects/" + project.getId()
        );
        emailService.sendReviewReceivedEmail(project.getStudent(), project.getTitle());

        return new ReviewResponse(
                savedReview.getId(),
                savedReview.getReviewerName(),
                reviewer.getEmail(),
                savedReview.getFeedback(),
                savedReview.getRating(),
                savedReview.getCreatedAt()
        );
    }
}
