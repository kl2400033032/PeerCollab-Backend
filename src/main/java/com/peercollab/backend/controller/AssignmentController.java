package com.peercollab.backend.controller;

import com.peercollab.backend.dto.assignment.AssignmentRequest;
import com.peercollab.backend.dto.assignment.AssignmentResponse;
import com.peercollab.backend.service.AssignmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse createAssignment(@Valid @RequestBody AssignmentRequest request) {
        return assignmentService.createAssignment(request);
    }

    @GetMapping
    public List<AssignmentResponse> getAssignmentsForAdmin() {
        return assignmentService.getAssignmentsForAdmin();
    }

    @GetMapping("/my")
    public List<AssignmentResponse> getAssignmentsForCurrentStudent() {
        return assignmentService.getAssignmentsForCurrentStudent();
    }
}
