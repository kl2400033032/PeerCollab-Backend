package com.peercollab.backend.dto.auth;

import com.peercollab.backend.entity.UserRole;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role
) {
}
