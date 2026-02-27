package com.revhire;

import com.revhire.config.CustomUserDetailsService;
import com.revhire.entity.Role;
import com.revhire.entity.User;
import com.revhire.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_ReturnsUserDetails() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded");
        user.setRole(Role.JOB_SEEKER);

        when(userRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("alice");

        assertEquals("alice", details.getUsername());
        assertEquals("ROLE_JOB_SEEKER", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_ThrowsWhenNotFound() {
        when(userRepository.findByUsernameIgnoreCase("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("missing"));
    }
}
