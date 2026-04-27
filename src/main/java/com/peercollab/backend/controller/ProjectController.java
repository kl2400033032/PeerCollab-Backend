package com.peercollab.backend.controller;

import com.peercollab.backend.dto.comment.CommentRequest;
import com.peercollab.backend.dto.comment.CommentResponse;
import com.peercollab.backend.dto.common.PagedResponse;
import com.peercollab.backend.dto.project.ProjectDetailsResponse;
import com.peercollab.backend.dto.project.ProjectRequest;
import com.peercollab.backend.dto.project.ProjectSummaryResponse;
import com.peercollab.backend.dto.review.ReviewRequest;
import com.peercollab.backend.dto.review.ReviewResponse;
import com.peercollab.backend.entity.ProjectStatus;
import com.peercollab.backend.service.CommentService;
import com.peercollab.backend.service.ProjectService;
import com.peercollab.backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ReviewService reviewService;
    private final CommentService commentService;

    public ProjectController(ProjectService projectService, ReviewService reviewService, CommentService commentService) {
        this.projectService = projectService;
        this.reviewService = reviewService;
        this.commentService = commentService;
    }

    @GetMapping
    public PagedResponse<ProjectSummaryResponse> getProjects(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(defaultValue = "") String studentName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        return projectService.getProjects(search, status, studentName, page, size);
    }

    @GetMapping("/mine")
    public java.util.List<ProjectSummaryResponse> getCurrentUserProjects() {
        return projectService.getCurrentUserProjects();
    }

    @GetMapping("/{id}")
    public ProjectDetailsResponse getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDetailsResponse createProject(@Valid @RequestBody ProjectRequest request) {
        return projectService.createProject(request);
    }

    @PutMapping("/{id}")
    public ProjectDetailsResponse updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return projectService.updateProject(id, request);
    }

    @PostMapping(path = "/{id}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProjectDetailsResponse uploadAttachment(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return projectService.uploadAttachment(id, file);
    }

    @GetMapping("/{id}/attachment/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) {
        ProjectService.DownloadedProjectFile downloadedFile = projectService.downloadAttachment(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(downloadedFile.fileName()).build());
        MediaType mediaType = downloadedFile.contentType() != null
                ? MediaType.parseMediaType(downloadedFile.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(downloadedFile.resource());
    }

    @PostMapping("/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse addReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return reviewService.addReview(id, request);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        return commentService.addComment(id, request);
    }
}
