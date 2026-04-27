package com.peercollab.backend.dto.assignment;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AssignmentRequest(
        @NotBlank(message = "Title is required.")
        @Size(min = 3, max = 140, message = "Title must be between 3 and 140 characters.")
        String title,
        @NotBlank(message = "Description is required.")
        @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters.")
        String description,
        @NotNull(message = "Due date is required.")
        @FutureOrPresent(message = "Due date must be today or later.")
        LocalDate dueDate,
        @NotNull(message = "Assigned student is required.")
        Long assignedStudentId
) {
}
