package com.peercollab.backend.controller;

import com.peercollab.backend.dto.auth.AuthRequest;
import com.peercollab.backend.dto.auth.AuthResponse;
import com.peercollab.backend.dto.auth.RegisterRequest;
import com.peercollab.backend.dto.auth.UserResponse;
import com.peercollab.backend.dto.common.ApiMessageResponse;
import com.peercollab.backend.security.AuthCookieService;
import com.peercollab.backend.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;
    private final String authMode;

    public AuthController(
            AuthService authService,
            AuthCookieService authCookieService,
            @Value("${app.auth.mode:cookie}") String authMode
    ) {
        this.authService = authService;
        this.authCookieService = authCookieService;
        this.authMode = authMode;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        if (usesBearerAuth()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(authCookieService.buildHeaderName(), authCookieService.buildAuthCookieHeader(response.token()))
                .body(response.withoutToken());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        if (usesBearerAuth()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok()
                .header(authCookieService.buildHeaderName(), authCookieService.buildAuthCookieHeader(response.token()))
                .body(response.withoutToken());
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return authService.getCurrentUser();
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken());
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiMessageResponse> logout() {
        if (usesBearerAuth()) {
            return ResponseEntity.ok().body(new ApiMessageResponse("Logout successful."));
        }
        return ResponseEntity.ok()
                .header(authCookieService.buildHeaderName(), authCookieService.buildLogoutCookieHeader())
                .body(new ApiMessageResponse("Logout successful."));
    }

    private boolean usesBearerAuth() {
        return "bearer".equalsIgnoreCase(authMode);
    }
}
