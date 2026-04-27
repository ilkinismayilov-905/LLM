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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return mapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return mapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(mapper::toUserResponse)
                .toList();
    }

    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("User already exists with email: " + request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // TODO: Encode password with BCrypt
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(Role.valueOf(request.role().toUpperCase()))
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());

        return mapper.toUserResponse(savedUser);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        if (request.isActive() != null) {
            user.setIsActive(request.isActive());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", updatedUser.getId());

        return mapper.toUserResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with id: {}", id);
    }
}

