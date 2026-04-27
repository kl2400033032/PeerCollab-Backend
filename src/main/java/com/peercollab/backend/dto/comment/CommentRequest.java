package com.peercollab.backend.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "Message is required.")
        @Size(min = 2, max = 1500, message = "Message must be between 2 and 1500 characters.")
        String message
) {
}
