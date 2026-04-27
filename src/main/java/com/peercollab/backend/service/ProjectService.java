package com.peercollab.backend.service;

import com.peercollab.backend.dto.comment.CommentResponse;
import com.peercollab.backend.dto.common.PagedResponse;
import com.peercollab.backend.dto.project.ProjectDetailsResponse;
import com.peercollab.backend.dto.project.ProjectFileResponse;
import com.peercollab.backend.dto.project.ProjectRequest;
import com.peercollab.backend.dto.project.ProjectSummaryResponse;
import com.peercollab.backend.dto.review.ReviewResponse;
import com.peercollab.backend.entity.ActivityType;
import com.peercollab.backend.entity.Assignment;
import com.peercollab.backend.entity.Comment;
import com.peercollab.backend.entity.Project;
import com.peercollab.backend.entity.ProjectStatus;
import com.peercollab.backend.entity.Review;
import com.peercollab.backend.entity.User;
import com.peercollab.backend.entity.UserRole;
import com.peercollab.backend.exception.BadRequestException;
import com.peercollab.backend.exception.ForbiddenException;
import com.peercollab.backend.exception.ResourceNotFoundException;
import com.peercollab.backend.repository.AssignmentRepository;
import com.peercollab.backend.repository.ProjectRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AssignmentRepository assignmentRepository;
    private final CurrentUserService currentUserService;
    private final FileStorageService fileStorageService;
    private final ActivityLogService activityLogService;

    public ProjectService(
            ProjectRepository projectRepository,
            AssignmentRepository assignmentRepository,
            CurrentUserService currentUserService,
            FileStorageService fileStorageService,
            ActivityLogService activityLogService
    ) {
        this.projectRepository = projectRepository;
        this.assignmentRepository = assignmentRepository;
        this.currentUserService = currentUserService;
        this.fileStorageService = fileStorageService;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    @Cacheable("projects")
    public PagedResponse<ProjectSummaryResponse> getProjects(String search, ProjectStatus status, String studentName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Specification<Project> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                predicates.add(builder.like(builder.lower(root.get("title")), "%" + search.trim().toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (studentName != null && !studentName.isBlank()) {
                predicates.add(builder.like(builder.lower(root.get("studentName")), "%" + studentName.trim().toLowerCase() + "%"));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProjectSummaryResponse> resultPage = projectRepository.findAll(specification, pageable).map(this::mapSummary);
        return new PagedResponse<>(
                resultPage.getContent(),
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.isFirst(),
                resultPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "projectDetails", key = "#id")
    public ProjectDetailsResponse getProjectById(Long id) {
        return mapDetails(findDetailedProject(id));
    }

    @Transactional(readOnly = true)
    public List<ProjectSummaryResponse> getCurrentUserProjects() {
        User currentUser = currentUserService.getCurrentUser();
        return projectRepository.findByStudent_Id(currentUser.getId())
                .stream()
                .sorted(Comparator.comparing(Project::getUpdatedAt).reversed())
                .map(this::mapSummary)
                .toList();
    }

    @CacheEvict(value = {"projects", "projectDetails", "studentProjects", "adminDashboard", "studentDashboard", "adminAnalytics"}, allEntries = true)
    public ProjectDetailsResponse createProject(ProjectRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != UserRole.STUDENT) {
            throw new ForbiddenException("Only students can create projects.");
        }

        Project project = new Project();
        applyRequest(project, request, currentUser);
        LocalDateTime now = LocalDateTime.now();
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        Project savedProject = projectRepository.save(project);
        activityLogService.log(currentUser, ActivityType.PROJECT_CREATED, "Created project " + savedProject.getTitle(), "projectId=" + savedProject.getId());
        return mapDetails(savedProject);
    }

    @CacheEvict(value = {"projects", "projectDetails", "studentProjects", "adminDashboard", "studentDashboard", "adminAnalytics"}, allEntries = true)
    public ProjectDetailsResponse updateProject(Long id, ProjectRequest request) {
        Project project = findProject(id);
        User currentUser = currentUserService.getCurrentUser();

        if (currentUser.getRole() == UserRole.STUDENT && !project.getStudent().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only update your own project.");
        }

        applyRequest(project, request, project.getStudent());
        project.setUpdatedAt(LocalDateTime.now());
        Project savedProject = projectRepository.save(project);
        activityLogService.log(currentUser, ActivityType.PROJECT_UPDATED, "Updated project " + savedProject.getTitle(), "projectId=" + savedProject.getId());
        return mapDetails(savedProject);
    }

    @CacheEvict(value = {"projects", "projectDetails", "studentProjects", "adminDashboard", "studentDashboard", "adminAnalytics"}, allEntries = true)
    public ProjectDetailsResponse uploadAttachment(Long id, MultipartFile file) {
        Project project = findProject(id);
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() == UserRole.STUDENT && !project.getStudent().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only upload files to your own project.");
        }

        FileStorageService.StoredFile storedFile = fileStorageService.store(file);
        project.setOriginalFileName(storedFile.originalFileName());
        project.setStoredFileName(storedFile.storedFileName());
        project.setFileContentType(storedFile.contentType());
        project.setFileSize(storedFile.size());
        project.setUpdatedAt(LocalDateTime.now());
        Project savedProject = projectRepository.save(project);
        activityLogService.log(currentUser, ActivityType.FILE_UPLOADED, "Uploaded a file for " + savedProject.getTitle(), "projectId=" + savedProject.getId());
        return mapDetails(savedProject);
    }

    @Transactional(readOnly = true)
    public DownloadedProjectFile downloadAttachment(Long id) {
        Project project = findDetailedProject(id);
        if (project.getStoredFileName() == null || project.getStoredFileName().isBlank()) {
            throw new ResourceNotFoundException("No file is attached to this project.");
        }
        Resource resource = fileStorageService.loadAsResource(project.getStoredFileName());
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Attached file could not be found.");
        }
        return new DownloadedProjectFile(resource, project.getOriginalFileName(), project.getFileContentType());
    }

    private void applyRequest(Project project, ProjectRequest request, User owner) {
        Assignment assignment = null;
        if (request.assignmentId() != null) {
            assignment = assignmentRepository.findById(request.assignmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found."));
            if (!assignment.getAssignedStudent().getId().equals(owner.getId())) {
                throw new ForbiddenException("This assignment is not assigned to the current student.");
            }
            projectRepository.findByAssignmentId(assignment.getId()).ifPresent(existingProject -> {
                if (project.getId() == null || !existingProject.getId().equals(project.getId())) {
                    throw new BadRequestException("A project already exists for this assignment.");
                }
            });
        }

        project.setTitle(request.title().trim());
        project.setDescription(request.description().trim());
        project.setStatus(request.status());
        project.setStudent(owner);
        project.setStudentName(owner.getName());
        project.setAssignment(assignment);
    }

    private Project findProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    private Project findDetailedProject(Long id) {
        return projectRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    private ProjectSummaryResponse mapSummary(Project project) {
        double average = project.getReviews().stream().mapToInt(Review::getRating).average().orElse(0.0);
        Double averageRating = project.getReviews().isEmpty() ? null : average;
        return new ProjectSummaryResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getStudentName(),
                project.getStudent().getEmail(),
                project.getStatus(),
                project.getReviews().size(),
                project.getComments().size(),
                averageRating,
                project.getAssignment() != null ? project.getAssignment().getId() : null,
                project.getAssignment() != null ? project.getAssignment().getTitle() : null,
                buildAttachment(project),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private ProjectDetailsResponse mapDetails(Project project) {
        List<ReviewResponse> reviews = project.getReviews()
                .stream()
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .map(review -> new ReviewResponse(
                        review.getId(),
                        review.getReviewerName(),
                        review.getReviewer().getEmail(),
                        review.getFeedback(),
                        review.getRating(),
                        review.getCreatedAt()
                ))
                .toList();

        List<CommentResponse> comments = project.getComments()
                .stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getAuthorName(),
                        comment.getAuthor().getEmail(),
                        comment.getMessage(),
                        comment.getCreatedAt()
                ))
                .toList();

        double average = project.getReviews().stream().mapToInt(Review::getRating).average().orElse(0.0);
        Double averageRating = project.getReviews().isEmpty() ? null : average;

        return new ProjectDetailsResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getStudentName(),
                project.getStudent().getEmail(),
                project.getStatus(),
                project.getAssignment() != null ? project.getAssignment().getId() : null,
                project.getAssignment() != null ? project.getAssignment().getTitle() : null,
                buildAttachment(project),
                reviews,
                comments,
                reviews.size(),
                comments.size(),
                averageRating,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private ProjectFileResponse buildAttachment(Project project) {
        if (project.getStoredFileName() == null || project.getStoredFileName().isBlank()) {
            return null;
        }
        return new ProjectFileResponse(
                project.getOriginalFileName(),
                project.getFileContentType(),
                project.getFileSize(),
                "/api/projects/" + project.getId() + "/attachment/download"
        );
    }

    public record DownloadedProjectFile(
            Resource resource,
            String fileName,
            String contentType
    ) {
    }
}
