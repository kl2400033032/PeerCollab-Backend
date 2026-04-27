package com.peercollab.backend.dto.assignment;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AssignmentResponse(
        Long id,
        String title,
        String description,
        LocalDate dueDate,
        Long assignedStudentId,
        String assignedStudentName,
        String createdByName,
        LocalDateTime createdAt,
        Long linkedProjectId
) {
}
