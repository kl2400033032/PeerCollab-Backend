package com.peercollab.backend.dto.auth;

public record AuthResponse(
        String token,
        String type,
        UserResponse user
) {
    public AuthResponse withoutToken() {
        return new AuthResponse(null, "HttpOnlyCookie", user);
    }
}
