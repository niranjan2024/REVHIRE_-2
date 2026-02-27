package com.revhire;

import com.revhire.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    void generateAndValidateToken_WorksForMatchingUser() {
        JwtService jwtService = new JwtService(SECRET, 60000L);
        UserDetails user = org.springframework.security.core.userdetails.User
                .withUsername("alice")
                .password("encoded")
                .authorities("ROLE_JOB_SEEKER")
                .build();

        String token = jwtService.generateToken(user, Map.of("role", "JOB_SEEKER"));

        assertEquals("alice", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_ReturnsFalseForDifferentUser() {
        JwtService jwtService = new JwtService(SECRET, 60000L);
        UserDetails alice = org.springframework.security.core.userdetails.User
                .withUsername("alice")
                .password("encoded")
                .authorities("ROLE_JOB_SEEKER")
                .build();
        UserDetails bob = org.springframework.security.core.userdetails.User
                .withUsername("bob")
                .password("encoded")
                .authorities("ROLE_JOB_SEEKER")
                .build();

        String token = jwtService.generateToken(alice, Map.of());

        assertFalse(jwtService.isTokenValid(token, bob));
    }

    @Test
    void extractUsername_ThrowsForInvalidToken() {
        JwtService jwtService = new JwtService(SECRET, 60000L);

        assertThrows(RuntimeException.class, () -> jwtService.extractUsername("not-a-jwt"));
    }
}
