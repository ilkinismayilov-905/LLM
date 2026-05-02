package com.example.service;

import com.example.dto.mapper.SubjectMapper;
import com.example.dto.request.CreateSubjectRequest;
import com.example.dto.request.UpdateSubjectRequest;
import com.example.dto.response.SubjectResponse;
import com.example.entity.Subject;
import com.example.exception.DuplicateUserException;
import com.example.exception.SubjectNotFoundException;
import com.example.repository.SubjectRepository;
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
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SubjectMapper mapper;

    @InjectMocks
    private SubjectService subjectService;

    private Subject subject;
    private SubjectResponse subjectResponse;

    @BeforeEach
    void setUp() {
        subject = Subject.builder()
                .id(1L)
                .name("Mathematics")
                .credits(5)
                .build();

        subjectResponse = SubjectResponse.builder()
                .id(1L)
                .name("Mathematics")
                .credits(5)
                .build();
    }

    @Test
    void shouldGetSubjectByIdSuccessfully() {
        // Arrange
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(mapper.toSubjectResponse(subject)).thenReturn(subjectResponse);

        // Act
        SubjectResponse result = subjectService.getSubjectById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Mathematics", result.name());
        assertEquals(5, result.credits());
        verify(subjectRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowSubjectNotFoundExceptionWhenGetByIdFails() {
        // Arrange
        when(subjectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SubjectNotFoundException.class, () -> subjectService.getSubjectById(999L));
        verify(subjectRepository, times(1)).findById(999L);
        verify(mapper, never()).toSubjectResponse(any());
    }

    @Test
    void shouldGetSubjectByNameSuccessfully() {
        // Arrange
        when(subjectRepository.findByName("Mathematics")).thenReturn(subject);
        when(mapper.toSubjectResponse(subject)).thenReturn(subjectResponse);

        // Act
        SubjectResponse result = subjectService.getSubjectByName("Mathematics");

        // Assert
        assertNotNull(result);
        assertEquals("Mathematics", result.name());
        verify(subjectRepository, times(1)).findByName("Mathematics");
    }

    @Test
    void shouldThrowSubjectNotFoundExceptionWhenGetByNameFails() {
        // Arrange
        when(subjectRepository.findByName("Unknown")).thenReturn(null);

        // Act & Assert
        assertThrows(SubjectNotFoundException.class, () -> subjectService.getSubjectByName("Unknown"));
        verify(subjectRepository, times(1)).findByName("Unknown");
        verify(mapper, never()).toSubjectResponse(any());
    }

    @Test
    void shouldGetAllSubjectsSuccessfully() {
        // Arrange
        List<Subject> subjects = Arrays.asList(subject);
        when(subjectRepository.findAll()).thenReturn(subjects);
        when(mapper.toSubjectResponse(subject)).thenReturn(subjectResponse);

        // Act
        List<SubjectResponse> result = subjectService.getAllSubjects();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Mathematics", result.get(0).name());
        verify(subjectRepository, times(1)).findAll();
    }

    @Test
    void shouldCreateSubjectSuccessfully() {
        // Arrange
        CreateSubjectRequest request = new CreateSubjectRequest("Physics", 4);
        Subject newSubject = Subject.builder().name("Physics").credits(4).build();
        Subject savedSubject = Subject.builder().id(2L).name("Physics").credits(4).build();
        SubjectResponse newResponse = SubjectResponse.builder().id(2L).name("Physics").credits(4).build();

        when(subjectRepository.existsByName(request.name())).thenReturn(false);
        when(subjectRepository.save(any(Subject.class))).thenReturn(savedSubject);
        when(mapper.toSubjectResponse(savedSubject)).thenReturn(newResponse);

        // Act
        SubjectResponse result = subjectService.createSubject(request);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("Physics", result.name());
        assertEquals(4, result.credits());
        verify(subjectRepository, times(1)).existsByName(request.name());
        verify(subjectRepository, times(1)).save(any(Subject.class));
    }

    @Test
    void shouldThrowDuplicateUserExceptionWhenCreatingExistingSubject() {
        // Arrange
        CreateSubjectRequest request = new CreateSubjectRequest("Mathematics", 5);
        when(subjectRepository.existsByName(request.name())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateUserException.class, () -> subjectService.createSubject(request));
        verify(subjectRepository, times(1)).existsByName(request.name());
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void shouldUpdateSubjectSuccessfully() {
        // Arrange
        UpdateSubjectRequest request = new UpdateSubjectRequest(6); // Yeni kredit sayı: 6
        Subject updatedSubject = Subject.builder().id(1L).name("Mathematics").credits(6).build();
        SubjectResponse updatedResponse = SubjectResponse.builder().id(1L).name("Mathematics").credits(6).build();

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(any(Subject.class))).thenReturn(updatedSubject);
        when(mapper.toSubjectResponse(updatedSubject)).thenReturn(updatedResponse);

        // Act
        SubjectResponse result = subjectService.updateSubject(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(6, result.credits()); // Kreditin 6-ya dəyişdiyini yoxlayırıq
        verify(subjectRepository, times(1)).findById(1L);
        verify(subjectRepository, times(1)).save(any(Subject.class));
    }

    @Test
    void shouldThrowSubjectNotFoundExceptionWhenUpdatingNonExistentSubject() {
        // Arrange
        UpdateSubjectRequest request = new UpdateSubjectRequest(6);
        when(subjectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SubjectNotFoundException.class, () -> subjectService.updateSubject(999L, request));
        verify(subjectRepository, times(1)).findById(999L);
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void shouldDeleteSubjectSuccessfully() {
        // Arrange
        when(subjectRepository.existsById(1L)).thenReturn(true);

        // Act
        subjectService.deleteSubject(1L);

        // Assert
        verify(subjectRepository, times(1)).existsById(1L);
        verify(subjectRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowSubjectNotFoundExceptionWhenDeletingNonExistentSubject() {
        // Arrange
        when(subjectRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(SubjectNotFoundException.class, () -> subjectService.deleteSubject(999L));
        verify(subjectRepository, times(1)).existsById(999L);
        verify(subjectRepository, never()).deleteById(anyLong());
    }
}