package com.example.service;

import com.example.dto.mapper.TeacherGroupSubjectMapper;
import com.example.dto.request.CreateTeacherGroupSubjectRequest;
import com.example.dto.response.*;
import com.example.entity.Group;
import com.example.entity.Subject;
import com.example.entity.Teacher;
import com.example.entity.TeacherGroupSubject;
import com.example.exception.GroupNotFoundException;
import com.example.exception.InvalidInputException;
import com.example.exception.SubjectNotFoundException;
import com.example.exception.TeacherNotFoundException;
import com.example.repository.GroupRepository;
import com.example.repository.SubjectRepository;
import com.example.repository.TeacherGroupSubjectRepository;
import com.example.repository.TeacherRepository;
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
class TeacherGroupSubjectServiceTest {

    @Mock
    private TeacherGroupSubjectRepository tgsRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TeacherGroupSubjectMapper mapper;

    @InjectMocks
    private TeacherGroupSubjectService teacherGroupSubjectService;

    private Teacher teacher;
    private Group group;
    private Subject subject;
    private TeacherGroupSubject teacherGroupSubject;
    private TeacherGroupSubjectResponse tgsResponse;

    @BeforeEach
    void setUp() {
        teacher = Teacher.builder().id(1L).build();
        group = Group.builder().id(1L).groupNumber("CS-101").build();
        subject = Subject.builder().id(1L).name("Mathematics").build();

        teacherGroupSubject = TeacherGroupSubject.builder()
                .id(1L)
                .teacher(teacher)
                .group(group)
                .subject(subject)
                .build();

        tgsResponse = TeacherGroupSubjectResponse.builder()
                .id(1L)
                .teacher(TeacherResponse.builder().id(1L).build())
                .group(GroupResponse.builder().id(1L).groupNumber("CS-101").build())
                .subject(SubjectResponse.builder().id(1L).name("Mathematics").build())
                .build();
    }

    @Test
    void shouldGetTeacherGroupSubjectByIdSuccessfully() {
        // Arrange
        when(tgsRepository.findById(1L)).thenReturn(Optional.of(teacherGroupSubject));
        when(mapper.toTeacherGroupSubjectResponse(teacherGroupSubject)).thenReturn(tgsResponse);

        // Act
        TeacherGroupSubjectResponse result = teacherGroupSubjectService.getTeacherGroupSubjectById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(tgsRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowInvalidInputExceptionWhenGetByIdFails() {
        // Arrange
        when(tgsRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidInputException.class, () -> teacherGroupSubjectService.getTeacherGroupSubjectById(999L));
        verify(tgsRepository, times(1)).findById(999L);
        verify(mapper, never()).toTeacherGroupSubjectResponse(any());
    }

    @Test
    void shouldGetAllTeacherGroupSubjectsSuccessfully() {
        // Arrange
        when(tgsRepository.findAll()).thenReturn(Arrays.asList(teacherGroupSubject));
        when(mapper.toTeacherGroupSubjectResponse(teacherGroupSubject)).thenReturn(tgsResponse);

        // Act
        List<TeacherGroupSubjectResponse> result = teacherGroupSubjectService.getAllTeacherGroupSubjects();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tgsRepository, times(1)).findAll();
    }

    @Test
    void shouldGetTeacherGroupSubjectsByTeacherIdSuccessfully() {
        // Arrange
        when(tgsRepository.findAllByTeacherId(1L)).thenReturn(Arrays.asList(teacherGroupSubject));
        when(mapper.toTeacherGroupSubjectResponse(teacherGroupSubject)).thenReturn(tgsResponse);

        // Act
        List<TeacherGroupSubjectResponse> result = teacherGroupSubjectService.getTeacherGroupSubjectsByTeacherId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tgsRepository, times(1)).findAllByTeacherId(1L);
    }

    @Test
    void shouldCreateTeacherGroupSubjectSuccessfully() {
        // Arrange
        CreateTeacherGroupSubjectRequest request = mock(CreateTeacherGroupSubjectRequest.class);
        when(request.teacherId()).thenReturn(1L);
        when(request.groupId()).thenReturn(1L);
        when(request.subjectId()).thenReturn(1L);

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(tgsRepository.existsByTeacherIdAndGroupIdAndSubjectId(1L, 1L, 1L)).thenReturn(false);
        when(tgsRepository.save(any(TeacherGroupSubject.class))).thenReturn(teacherGroupSubject);
        when(mapper.toTeacherGroupSubjectResponse(teacherGroupSubject)).thenReturn(tgsResponse);

        // Act
        TeacherGroupSubjectResponse result = teacherGroupSubjectService.createTeacherGroupSubject(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(teacherRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).findById(1L);
        verify(subjectRepository, times(1)).findById(1L);
        verify(tgsRepository, times(1)).save(any(TeacherGroupSubject.class));
    }

    @Test
    void shouldThrowTeacherNotFoundExceptionWhenCreatingWithInvalidTeacher() {
        // Arrange
        CreateTeacherGroupSubjectRequest request = mock(CreateTeacherGroupSubjectRequest.class);
        when(request.teacherId()).thenReturn(999L);

        when(teacherRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TeacherNotFoundException.class, () -> teacherGroupSubjectService.createTeacherGroupSubject(request));
        verify(teacherRepository, times(1)).findById(999L);
        verify(tgsRepository, never()).save(any(TeacherGroupSubject.class));
    }

    @Test
    void shouldThrowGroupNotFoundExceptionWhenCreatingWithInvalidGroup() {
        // Arrange
        CreateTeacherGroupSubjectRequest request = mock(CreateTeacherGroupSubjectRequest.class);
        when(request.teacherId()).thenReturn(1L);
        when(request.groupId()).thenReturn(999L);

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(GroupNotFoundException.class, () -> teacherGroupSubjectService.createTeacherGroupSubject(request));
        verify(groupRepository, times(1)).findById(999L);
        verify(tgsRepository, never()).save(any(TeacherGroupSubject.class));
    }

    @Test
    void shouldThrowSubjectNotFoundExceptionWhenCreatingWithInvalidSubject() {
        // Arrange
        CreateTeacherGroupSubjectRequest request = mock(CreateTeacherGroupSubjectRequest.class);
        when(request.teacherId()).thenReturn(1L);
        when(request.groupId()).thenReturn(1L);
        when(request.subjectId()).thenReturn(999L);

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(subjectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SubjectNotFoundException.class, () -> teacherGroupSubjectService.createTeacherGroupSubject(request));
        verify(subjectRepository, times(1)).findById(999L);
        verify(tgsRepository, never()).save(any(TeacherGroupSubject.class));
    }

    @Test
    void shouldThrowInvalidInputExceptionWhenAssignmentAlreadyExists() {
        // Arrange
        CreateTeacherGroupSubjectRequest request = mock(CreateTeacherGroupSubjectRequest.class);
        when(request.teacherId()).thenReturn(1L);
        when(request.groupId()).thenReturn(1L);
        when(request.subjectId()).thenReturn(1L);

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(tgsRepository.existsByTeacherIdAndGroupIdAndSubjectId(1L, 1L, 1L)).thenReturn(true);

        // Act & Assert
        assertThrows(InvalidInputException.class, () -> teacherGroupSubjectService.createTeacherGroupSubject(request));
        verify(tgsRepository, times(1)).existsByTeacherIdAndGroupIdAndSubjectId(1L, 1L, 1L);
        verify(tgsRepository, never()).save(any(TeacherGroupSubject.class));
    }

    @Test
    void shouldDeleteTeacherGroupSubjectSuccessfully() {
        // Arrange
        when(tgsRepository.existsById(1L)).thenReturn(true);

        // Act
        teacherGroupSubjectService.deleteTeacherGroupSubject(1L);

        // Assert
        verify(tgsRepository, times(1)).existsById(1L);
        verify(tgsRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowInvalidInputExceptionWhenDeletingNonExistentAssignment() {
        // Arrange
        when(tgsRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidInputException.class, () -> teacherGroupSubjectService.deleteTeacherGroupSubject(999L));
        verify(tgsRepository, times(1)).existsById(999L);
        verify(tgsRepository, never()).deleteById(anyLong());
    }
}