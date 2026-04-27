package com.peercollab.backend.dto.project;

public record ProjectFileResponse(
        String fileName,
        String contentType,
        Long fileSize,
        String downloadUrl
) {
}
