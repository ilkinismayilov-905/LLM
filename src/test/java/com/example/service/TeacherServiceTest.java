package com.example.service;

import com.example.dto.mapper.TeacherMapper;
import com.example.dto.request.CreateTeacherRequest;
import com.example.dto.request.UpdateTeacherRequest;
import com.example.dto.response.TeacherResponse;
import com.example.entity.Teacher;
import com.example.entity.User;
import com.example.enums.Department;
import com.example.exception.TeacherNotFoundException;
import com.example.exception.UserNotFoundException;
import com.example.repository.TeacherRepository;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeacherMapper mapper;

    @InjectMocks
    private TeacherService teacherService;

    private User user;
    private Teacher teacher;
    private TeacherResponse teacherResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("teacher@school.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        teacher = Teacher.builder()
                .id(10L)
                .user(user)
                .department(Department.MATHEMATICS)
                .build();

        teacherResponse = TeacherResponse.builder()
                .id(10L)
                .department(Department.MATHEMATICS)
                // Digər sahələri ehtiyac olduqca əlavə edə bilərsiniz
                .build();
    }

    @Test
    void shouldGetTeacherByIdSuccessfully() {
        // Arrange
        when(teacherRepository.findById(10L)).thenReturn(Optional.of(teacher));
        when(mapper.toTeacherResponse(teacher)).thenReturn(teacherResponse);

        // Act
        TeacherResponse result = teacherService.getTeacherById(10L);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.id());
        verify(teacherRepository, times(1)).findById(10L);
    }

    @Test
    void shouldThrowTeacherNotFoundExceptionWhenGetByIdFails() {
        // Arrange
        when(teacherRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TeacherNotFoundException.class, () -> teacherService.getTeacherById(999L));
        verify(teacherRepository, times(1)).findById(999L);
        verify(mapper, never()).toTeacherResponse(any());
    }

    @Test
    void shouldGetTeacherByUserIdSuccessfully() {
        // Arrange
        when(teacherRepository.findByUserId(1L)).thenReturn(Optional.of(teacher));
        when(mapper.toTeacherResponse(teacher)).thenReturn(teacherResponse);

        // Act
        TeacherResponse result = teacherService.getTeacherByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.id());
        verify(teacherRepository, times(1)).findByUserId(1L);
    }

    @Test
    void shouldThrowTeacherNotFoundExceptionWhenGetByUserIdFails() {
        // Arrange
        when(teacherRepository.findByUserId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TeacherNotFoundException.class, () -> teacherService.getTeacherByUserId(999L));
        verify(teacherRepository, times(1)).findByUserId(999L);
        verify(mapper, never()).toTeacherResponse(any());
    }

    @Test
    void shouldGetAllTeachersSuccessfully() {
        // Arrange
        when(teacherRepository.findAll()).thenReturn(Arrays.asList(teacher));
        when(mapper.toTeacherResponse(teacher)).thenReturn(teacherResponse);

        // Act
        List<TeacherResponse> result = teacherService.getAllTeachers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(teacherRepository, times(1)).findAll();
    }

    @Test
    void shouldCreateTeacherSuccessfully() {
        // Arrange
        CreateTeacherRequest request = mock(CreateTeacherRequest.class);
        when(request.userId()).thenReturn(1L);
        when(request.department()).thenReturn(Department.MATHEMATICS);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);
        when(mapper.toTeacherResponse(teacher)).thenReturn(teacherResponse);

        // Act
        TeacherResponse result = teacherService.createTeacher(request);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.id());
        verify(userRepository, times(1)).findById(1L);
        verify(teacherRepository, times(1)).save(any(Teacher.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenCreatingWithInvalidUserId() {
        // Arrange
        CreateTeacherRequest request = mock(CreateTeacherRequest.class);
        when(request.userId()).thenReturn(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> teacherService.createTeacher(request));
        verify(userRepository, times(1)).findById(999L);
        verify(teacherRepository, never()).save(any(Teacher.class));
    }

    @Test
    void shouldUpdateTeacherSuccessfully() {
        // Arrange
        UpdateTeacherRequest request = mock(UpdateTeacherRequest.class);
        when(request.department()).thenReturn(Department.PHYSICS); // Fərqli bir departament

        Teacher updatedTeacher = Teacher.builder()
                .id(10L)
                .user(user)
                .department(Department.PHYSICS)
                .build();

        TeacherResponse updatedResponse = TeacherResponse.builder()
                .id(10L)
                .department(Department.PHYSICS)
                .build();

        when(teacherRepository.findById(10L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(updatedTeacher);
        when(mapper.toTeacherResponse(updatedTeacher)).thenReturn(updatedResponse);

        // Act
        TeacherResponse result = teacherService.updateTeacher(10L, request);

        // Assert
        assertNotNull(result);
        assertEquals(Department.PHYSICS, result.department()); // Departamentin dəyişdiyini yoxlayırıq
        verify(teacherRepository, times(1)).findById(10L);
        verify(teacherRepository, times(1)).save(any(Teacher.class));
    }

    @Test
    void shouldThrowTeacherNotFoundExceptionWhenUpdatingNonExistentTeacher() {
        // Arrange
        UpdateTeacherRequest request = mock(UpdateTeacherRequest.class);
        when(teacherRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TeacherNotFoundException.class, () -> teacherService.updateTeacher(999L, request));
        verify(teacherRepository, times(1)).findById(999L);
        verify(teacherRepository, never()).save(any(Teacher.class));
    }

    @Test
    void shouldDeleteTeacherSuccessfully() {
        // Arrange
        when(teacherRepository.existsById(10L)).thenReturn(true);

        // Act
        teacherService.deleteTeacher(10L);

        // Assert
        verify(teacherRepository, times(1)).existsById(10L);
        verify(teacherRepository, times(1)).deleteById(10L);
    }

    @Test
    void shouldThrowTeacherNotFoundExceptionWhenDeletingNonExistentTeacher() {
        // Arrange
        when(teacherRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(TeacherNotFoundException.class, () -> teacherService.deleteTeacher(999L));
        verify(teacherRepository, times(1)).existsById(999L);
        verify(teacherRepository, never()).deleteById(anyLong());
    }
}