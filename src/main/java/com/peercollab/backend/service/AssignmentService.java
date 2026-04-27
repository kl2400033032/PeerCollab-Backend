package com.peercollab.backend.service;

import com.peercollab.backend.dto.assignment.AssignmentRequest;
import com.peercollab.backend.dto.assignment.AssignmentResponse;
import com.peercollab.backend.entity.ActivityType;
import com.peercollab.backend.entity.Assignment;
import com.peercollab.backend.entity.NotificationType;
import com.peercollab.backend.entity.User;
import com.peercollab.backend.entity.UserRole;
import com.peercollab.backend.exception.BadRequestException;
import com.peercollab.backend.exception.ResourceNotFoundException;
import com.peercollab.backend.repository.AssignmentRepository;
import com.peercollab.backend.repository.ProjectRepository;
import com.peercollab.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            CurrentUserService currentUserService,
            NotificationService notificationService,
            ActivityLogService activityLogService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    @CacheEvict(value = {"adminDashboard", "studentDashboard", "adminAnalytics", "notifications"}, allEntries = true)
    public AssignmentResponse createAssignment(AssignmentRequest request) {
        User admin = currentUserService.getCurrentUser();
        User assignedStudent = userRepository.findById(request.assignedStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assigned student not found."));

        if (assignedStudent.getRole() != UserRole.STUDENT) {
            throw new BadRequestException("Assignments can only be assigned to students.");
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(request.title().trim());
        assignment.setDescription(request.description().trim());
        assignment.setDueDate(request.dueDate());
        assignment.setAssignedStudent(assignedStudent);
        assignment.setCreatedBy(admin);
        assignment.setCreatedAt(LocalDateTime.now());

        Assignment savedAssignment = assignmentRepository.save(assignment);
        activityLogService.log(admin, ActivityType.ASSIGNMENT_CREATED, "Created assignment " + savedAssignment.getTitle(), "assignmentId=" + savedAssignment.getId());
        notificationService.createNotification(
                assignedStudent,
                NotificationType.ASSIGNMENT_CREATED,
                "New assignment assigned",
                admin.getName() + " assigned you \"" + savedAssignment.getTitle() + "\".",
                "/dashboard"
        );
        return map(savedAssignment);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsForAdmin() {
        return assignmentRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsForCurrentStudent() {
        User currentUser = currentUserService.getCurrentUser();
        return assignmentRepository.findByAssignedStudent_IdOrderByDueDateAsc(currentUser.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    private AssignmentResponse map(Assignment assignment) {
        Long linkedProjectId = projectRepository.findByAssignmentId(assignment.getId())
                .map(project -> project.getId())
                .orElse(null);
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate(),
                assignment.getAssignedStudent().getId(),
                assignment.getAssignedStudent().getName(),
                assignment.getCreatedBy().getName(),
                assignment.getCreatedAt(),
                linkedProjectId
        );
    }
}
