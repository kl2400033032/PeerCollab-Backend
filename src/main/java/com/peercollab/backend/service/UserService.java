package com.peercollab.backend.service;

import com.peercollab.backend.dto.auth.UserResponse;
import com.peercollab.backend.entity.UserRole;
import com.peercollab.backend.repository.UserRepository;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable("students")
    public List<UserResponse> getStudents() {
        return userRepository.findByRoleOrderByNameAsc(UserRole.STUDENT)
                .stream()
                .map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole()))
                .toList();
    }
}
