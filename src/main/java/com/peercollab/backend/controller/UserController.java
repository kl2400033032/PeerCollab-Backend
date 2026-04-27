package com.peercollab.backend.controller;

import com.peercollab.backend.dto.auth.UserResponse;
import com.peercollab.backend.service.UserService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/students")
    public List<UserResponse> getStudents() {
        return userService.getStudents();
    }
}
