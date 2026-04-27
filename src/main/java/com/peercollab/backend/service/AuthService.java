package com.peercollab.backend.service;

import com.peercollab.backend.dto.auth.AuthRequest;
import com.peercollab.backend.dto.auth.AuthResponse;
import com.peercollab.backend.dto.auth.RegisterRequest;
import com.peercollab.backend.dto.auth.UserResponse;
import com.peercollab.backend.entity.ActivityType;
import com.peercollab.backend.entity.User;
import com.peercollab.backend.exception.BadRequestException;
import com.peercollab.backend.repository.UserRepository;
import com.peercollab.backend.security.JwtService;
import java.time.LocalDateTime;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;
    private final ActivityLogService activityLogService;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService,
            CurrentUserService currentUserService,
            ActivityLogService activityLogService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.currentUserService = currentUserService;
        this.activityLogService = activityLogService;
        this.emailService = emailService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("Email is already registered.");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        activityLogService.log(savedUser, ActivityType.REGISTER, "Registered a new account", null);
        emailService.sendRegistrationEmail(savedUser);

        return buildAuthResponse(savedUser.getEmail());
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("User could not be loaded."));
        activityLogService.log(user, ActivityType.LOGIN, "Signed in to PeerCollab", null);
        return buildAuthResponse(request.email().trim().toLowerCase());
    }

    public UserResponse getCurrentUser() {
        return mapUser(currentUserService.getCurrentUser());
    }

    private AuthResponse buildAuthResponse(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("User could not be loaded."));
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, "Bearer", mapUser(user));
    }

    private UserResponse mapUser(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
