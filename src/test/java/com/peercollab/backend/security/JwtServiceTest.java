package com.peercollab.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void shouldGenerateAndValidateToken() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "VGhpc0lzQVN0cm9uZ0RlbW9LZXlGb3JQZWVyQ29sbGFiSldUU2VjdXJpdHkyMDI2");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86_400_000L);

        User user = new User("architect@peercollab.com", "password", java.util.List.of());
        String token = jwtService.generateToken(user);

        assertEquals("architect@peercollab.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }
}
