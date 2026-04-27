package com.peercollab.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peercollab.backend.entity.User;
import com.peercollab.backend.entity.UserRole;
import com.peercollab.backend.repository.ActivityLogRepository;
import com.peercollab.backend.repository.AssignmentRepository;
import com.peercollab.backend.repository.CommentRepository;
import com.peercollab.backend.repository.NotificationRepository;
import com.peercollab.backend.repository.ProjectRepository;
import com.peercollab.backend.repository.ReviewRepository;
import com.peercollab.backend.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PeerCollabApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        activityLogRepository.deleteAll();
        reviewRepository.deleteAll();
        commentRepository.deleteAll();
        projectRepository.deleteAll();
        assignmentRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(createUser("Admin User", "admin@test.com", "Admin@123", UserRole.ADMIN));
        userRepository.save(createUser("Student One", "student1@test.com", "Student@123", UserRole.STUDENT));
        userRepository.save(createUser("Student Two", "student2@test.com", "Student@123", UserRole.STUDENT));
    }

    @Test
    void shouldRegisterUserAndReturnJwt() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Demo Student",
                                  "email": "demo.student@test.com",
                                  "password": "Student@123",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("HttpOnlyCookie"))
                .andExpect(jsonPath("$.user.email").value("demo.student@test.com"))
                .andExpect(jsonPath("$.user.role").value("STUDENT"));
    }

    @Test
    void shouldCreateProjectThenReviewCommentAndNotifyOwner() throws Exception {
        Cookie studentOneCookie = loginAndGetCookie("student1@test.com", "Student@123");
        Cookie studentTwoCookie = loginAndGetCookie("student2@test.com", "Student@123");
        Cookie adminCookie = loginAndGetCookie("admin@test.com", "Admin@123");

        String createResponse = mockMvc.perform(post("/api/projects")
                        .with(csrf())
                        .cookie(studentOneCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Integration Demo Project",
                                  "description": "A fully tested project workflow for the final PeerCollab demo.",
                                  "status": "SUBMITTED",
                                  "assignmentId": null
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Demo Project"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdProject = objectMapper.readTree(createResponse);
        long projectId = createdProject.get("id").asLong();

        mockMvc.perform(post("/api/projects/{id}/reviews", projectId)
                        .with(csrf())
                        .cookie(studentTwoCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "feedback": "Excellent final integration flow with clear collaboration steps.",
                                  "rating": 5
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5));

        mockMvc.perform(post("/api/projects/{id}/comments", projectId)
                        .with(csrf())
                        .cookie(studentTwoCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "The collaboration timeline is ready for the demo."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorEmail").value("student2@test.com"));

        mockMvc.perform(get("/api/notifications/summary")
                        .cookie(studentOneCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(2));

        mockMvc.perform(get("/api/analytics/admin")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(3))
                .andExpect(jsonPath("$.totalProjects").value(1));

        assertThat(notificationRepository.count()).isEqualTo(2);
        assertThat(activityLogRepository.count()).isGreaterThanOrEqualTo(3);
    }

    private User createUser(String name, String email, String password, UserRole role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private Cookie loginAndGetCookie(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("peercollab_access_token");
    }
}
