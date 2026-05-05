package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccessServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAccessServiceImpl userLoginService;

    @Test
    void loginShouldReturnUserWhenPasswordMatches() {
        String stored = "$2a$10$stored";
        User   user   = new User(1L, "alice", null, stored, User.UserRole.USER, LocalDateTime.now());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", stored)).thenReturn(true);

        Optional<User> result = userLoginService.login("alice", "secret123");

        assertTrue(result.isPresent());
    }

    @Test
    void loginShouldReturnEmptyWhenPasswordDoesNotMatch() {
        String stored = "$2a$10$stored";
        User   user   = new User(1L, "alice", null, stored, User.UserRole.USER, LocalDateTime.now());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", stored)).thenReturn(false);

        Optional<User> result = userLoginService.login("alice", "wrong");

        assertFalse(result.isPresent());
    }

    @Test
    void loginShouldReturnEmptyWhenUsernameIsNull() {
        assertFalse(userLoginService.login(null, "secret123").isPresent());
    }

    @Test
    void loginShouldReturnEmptyWhenPasswordIsBlank() {
        Optional<User> result = userLoginService.login("alice", "");

        assertFalse(result.isPresent());
    }

    @Test
    void registerShouldPersistEncryptedPassword() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<User> result = userLoginService.register("alice", "secret123");

        assertTrue(result.isPresent());
        assertNotEquals("secret123", result.get().getPassword());
        assertEquals("$2a$10$encoded", result.get().getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerShouldReturnEmptyWhenPasswordIsBlank() {
        Optional<User> result = userLoginService.register("alice", "   ");

        assertFalse(result.isPresent());
    }

    @Test
    void registerShouldReturnEmptyWhenUsernameExists() {
        User user = new User(1L, "alice", null, "$2a$10$encoded", User.UserRole.USER, LocalDateTime.now());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        Optional<User> result = userLoginService.register("alice", "secret123");

        assertFalse(result.isPresent());
    }
}
