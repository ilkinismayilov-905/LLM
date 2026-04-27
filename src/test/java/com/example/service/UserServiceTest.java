package com.example.service;

import com.example.dto.mapper.UserMapper;
import com.example.dto.request.CreateUserRequest;
import com.example.dto.request.UpdateUserRequest;
import com.example.dto.response.UserResponse;
import com.example.entity.User;
import com.example.enums.Role;
import com.example.exception.DuplicateUserException;
import com.example.exception.UserNotFoundException;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private CreateUserRequest createUserRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@school.com")
                .password("encodedPassword123")
                .firstName("John")
                .lastName("Doe")
                .role(Role.TEACHER)
                .isActive(true)
                .build();

        createUserRequest = new CreateUserRequest(
                "test@school.com",
                "PlainPassword123",
                "John",
                "Doe",
                "TEACHER"
        );

        userResponse = new UserResponse(
                1L,
                "test@school.com",
                "John",
                "Doe",
                "TEACHER",
                true
        );
    }

    @Test
    void shouldGetAllUsersSuccessfully() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        List<UserResponse> responses = Arrays.asList(userResponse);
        when(userRepository.findAll()).thenReturn(users);
        when(mapper.toUserResponse(testUser)).thenReturn(userResponse);

        // Act
        List<UserResponse> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userResponse.email(), result.get(0).email());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(mapper.toUserResponse(testUser)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(userResponse.email(), result.email());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(999L));
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void shouldCreateUserSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail(createUserRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(createUserRequest.password())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(mapper.toUserResponse(testUser)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.createUser(createUserRequest);

        // Assert
        assertNotNull(result);
        assertEquals(userResponse.email(), result.email());
        verify(userRepository, times(1)).existsByEmail(createUserRequest.email());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(createUserRequest.password());
    }

    @Test
    void shouldThrowDuplicateUserExceptionWhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(createUserRequest.email())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateUserException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository, times(1)).existsByEmail(createUserRequest.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        // Arrange
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .email("test@school.com")
                .password("encodedPassword123")
                .firstName("Jane")
                .lastName("Smith")
                .role(Role.STUDENT)
                .isActive(true)
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(1L)
                .email("test@school.com")
                .firstName("Jane")
                .lastName("Smith")
                .role("STUDENT")
                .isActive(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(mapper.toUserResponse(updatedUser)).thenReturn(updatedResponse);

        // Act
        UserResponse result = userService.updateUser(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.firstName());
        assertEquals("STUDENT", result.role());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUpdatingNonExistentUser() {
        // Arrange
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .firstName("Jane")
                .build();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(999L, updateRequest));
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenDeletingNonExistentUser() {
        // Arrange
        when(userRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(999L));
        verify(userRepository, times(1)).existsById(999L);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void shouldGetUserByEmailSuccessfully() {
        // Arrange
        when(userRepository.findByEmail("test@school.com")).thenReturn(Optional.of(testUser));
        when(mapper.toUserResponse(testUser)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.getUserByEmail("test@school.com");

        // Assert
        assertNotNull(result);
        assertEquals(userResponse.email(), result.email());
        verify(userRepository, times(1)).findByEmail("test@school.com");
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenEmailDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@school.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("nonexistent@school.com"));
        verify(userRepository, times(1)).findByEmail("nonexistent@school.com");
    }
}

