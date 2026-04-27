package com.peercollab.backend.service;

import com.peercollab.backend.dto.comment.CommentRequest;
import com.peercollab.backend.dto.comment.CommentResponse;
import com.peercollab.backend.dto.realtime.RealtimeCommentMessage;
import com.peercollab.backend.entity.ActivityType;
import com.peercollab.backend.entity.Comment;
import com.peercollab.backend.entity.NotificationType;
import com.peercollab.backend.entity.Project;
import com.peercollab.backend.entity.User;
import com.peercollab.backend.exception.ResourceNotFoundException;
import com.peercollab.backend.repository.CommentRepository;
import com.peercollab.backend.repository.ProjectRepository;
import java.time.LocalDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;
    private final SimpMessagingTemplate messagingTemplate;

    public CommentService(
            CommentRepository commentRepository,
            ProjectRepository projectRepository,
            CurrentUserService currentUserService,
            NotificationService notificationService,
            ActivityLogService activityLogService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.commentRepository = commentRepository;
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
        this.messagingTemplate = messagingTemplate;
    }

    @CacheEvict(value = {"projects", "projectDetails", "studentProjects", "adminDashboard", "studentDashboard", "adminAnalytics", "notifications"}, allEntries = true)
    public CommentResponse addComment(Long projectId, CommentRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        User author = currentUserService.getCurrentUser();

        Comment comment = new Comment();
        comment.setProject(project);
        comment.setAuthor(author);
        comment.setAuthorName(author.getName());
        comment.setMessage(request.message().trim());
        comment.setCreatedAt(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        CommentResponse response = new CommentResponse(
                savedComment.getId(),
                savedComment.getAuthorName(),
                author.getEmail(),
                savedComment.getMessage(),
                savedComment.getCreatedAt()
        );

        activityLogService.log(author, ActivityType.COMMENT_ADDED, "Commented on " + project.getTitle(), "projectId=" + project.getId());
        if (!project.getStudent().getId().equals(author.getId())) {
            notificationService.createNotification(
                    project.getStudent(),
                    NotificationType.COMMENT_RECEIVED,
                    "New project comment",
                    author.getName() + " commented on your project \"" + project.getTitle() + "\".",
                    "/projects/" + project.getId()
            );
        }
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/comments", new RealtimeCommentMessage("COMMENT_CREATED", projectId, response));
        return response;
    }
}
