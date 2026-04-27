package com.example.service;

import com.example.dto.mapper.StudentMapper;
import com.example.dto.request.CreateStudentRequest;
import com.example.dto.request.UpdateStudentRequest;
import com.example.dto.response.StudentResponse;
import com.example.entity.*;
import com.example.enums.Role;
import com.example.exception.*;
import com.example.repository.GroupRepository;
import com.example.repository.StudentRepository;
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
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private StudentMapper mapper;

    @InjectMocks
    private StudentService studentService;

    private User user;
    private Student student;
    private Group group;
    private StudentResponse studentResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("student@school.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.STUDENT)
                .build();

        group = Group.builder()
                .id(1L)
                .groupNumber("CS-101")
                .build();

        student = Student.builder()
                .id(1L)
                .user(user)
                .studentNumber("STU001")
                .group(group)
                .build();

        studentResponse = StudentResponse.builder()
                .id(1L)
                .studentNumber("STU001")
                .build();
    }

    @Test
    void shouldGetAllStudentsSuccessfully() {
        // Arrange
        List<Student> students = Arrays.asList(student);
        when(studentRepository.findAll()).thenReturn(students);
        when(mapper.toStudentResponse(student)).thenReturn(studentResponse);

        // Act
        List<StudentResponse> result = studentService.getAllStudents();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    void shouldGetStudentByIdSuccessfully() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(mapper.toStudentResponse(student)).thenReturn(studentResponse);

        // Act
        StudentResponse result = studentService.getStudentById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(studentResponse.studentNumber(), result.studentNumber());
        verify(studentRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowStudentNotFoundExceptionWhenStudentDoesNotExist() {
        // Arrange
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StudentNotFoundException.class, () -> studentService.getStudentById(999L));
    }

    @Test
    void shouldGetStudentByStudentNumberSuccessfully() {
        // Arrange
        when(studentRepository.findByStudentNumber("STU001")).thenReturn(Optional.of(student));
        when(mapper.toStudentResponse(student)).thenReturn(studentResponse);

        // Act
        StudentResponse result = studentService.getStudentByStudentNumber("STU001");

        // Assert
        assertNotNull(result);
        assertEquals(studentResponse.studentNumber(), result.studentNumber());
    }

    @Test
    void shouldCreateStudentSuccessfully() {
        // Arrange
        CreateStudentRequest request = CreateStudentRequest.builder()
                .userId(1L)
                .studentNumber("STU001")
                .groupId(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(studentRepository.existsByStudentNumber("STU001")).thenReturn(false);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(mapper.toStudentResponse(student)).thenReturn(studentResponse);

        // Act
        StudentResponse result = studentService.createStudent(request);

        // Assert
        assertNotNull(result);
        assertEquals(studentResponse.studentNumber(), result.studentNumber());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void shouldThrowDuplicateStudentExceptionWhenStudentNumberAlreadyExists() {
        // Arrange
        CreateStudentRequest request = CreateStudentRequest.builder()
                .userId(1L)
                .studentNumber("STU001")
                .groupId(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(studentRepository.existsByStudentNumber("STU001")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateStudentException.class, () -> studentService.createStudent(request));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenCreatingStudent() {
        // Arrange
        CreateStudentRequest request = CreateStudentRequest.builder()
                .userId(999L)
                .studentNumber("STU001")
                .groupId(1L)
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> studentService.createStudent(request));
    }

    @Test
    void shouldThrowGroupNotFoundExceptionWhenCreatingStudent() {
        // Arrange
        CreateStudentRequest request = CreateStudentRequest.builder()
                .userId(1L)
                .studentNumber("STU001")
                .groupId(999L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(GroupNotFoundException.class, () -> studentService.createStudent(request));

        verify(studentRepository, never()).existsByStudentNumber(anyString());
    }

    @Test
    void shouldUpdateStudentSuccessfully() {
        // Arrange
        UpdateStudentRequest updateRequest = UpdateStudentRequest.builder()
                .groupId(2L)
                .build();

        Group newGroup = Group.builder()
                .id(2L)
                .groupNumber("CS-102")
                .build();

        Student updatedStudent = Student.builder()
                .id(1L)
                .user(user)
                .studentNumber("STU001")
                .group(newGroup)
                .build();

        StudentResponse updatedResponse = StudentResponse.builder()
                .id(1L)
                .studentNumber("STU001")
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(groupRepository.findById(2L)).thenReturn(Optional.of(newGroup));
        when(studentRepository.save(any(Student.class))).thenReturn(updatedStudent);
        when(mapper.toStudentResponse(updatedStudent)).thenReturn(updatedResponse);

        // Act
        StudentResponse result = studentService.updateStudent(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void shouldThrowStudentNotFoundExceptionWhenUpdatingNonExistentStudent() {
        // Arrange
        UpdateStudentRequest updateRequest = UpdateStudentRequest.builder()
                .groupId(2L)
                .build();

        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StudentNotFoundException.class, () -> studentService.updateStudent(999L, updateRequest));
    }

    @Test
    void shouldDeleteStudentSuccessfully() {
        // Arrange
        when(studentRepository.existsById(1L)).thenReturn(true);

        // Act
        studentService.deleteStudent(1L);

        // Assert
        verify(studentRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowStudentNotFoundExceptionWhenDeletingNonExistentStudent() {
        // Arrange
        when(studentRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(StudentNotFoundException.class, () -> studentService.deleteStudent(999L));
    }

    @Test
    void shouldGetStudentsByGroupIdSuccessfully() {
        // Arrange
        List<Student> students = Arrays.asList(student);
        when(studentRepository.findAllByGroupId(1L)).thenReturn(students);
        when(mapper.toStudentResponse(student)).thenReturn(studentResponse);

        // Act
        List<StudentResponse> result = studentService.getStudentsByGroupId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(studentRepository, times(1)).findAllByGroupId(1L);
    }

    @Test
    void shouldGetStudentByUserIdSuccessfully() {
        // Arrange
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(student));
        when(mapper.toStudentResponse(student)).thenReturn(studentResponse);

        // Act
        StudentResponse result = studentService.getStudentByUserId(1L);

        // Assert
        assertNotNull(result);
        verify(studentRepository, times(1)).findByUserId(1L);
    }

    @Test
    void shouldThrowStudentNotFoundExceptionWhenGettingByNonExistentUserId() {
        // Arrange
        when(studentRepository.findByUserId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StudentNotFoundException.class, () -> studentService.getStudentByUserId(999L));
    }
}

