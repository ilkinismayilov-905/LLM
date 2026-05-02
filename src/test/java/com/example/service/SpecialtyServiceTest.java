package com.example.service;

import com.example.dto.mapper.SpecialtyMapper;
import com.example.dto.request.CreateSpecialtyRequest;
import com.example.dto.request.UpdateSpecialtyRequest;
import com.example.dto.response.SpecialtyResponse;
import com.example.entity.Specialty;
import com.example.exception.DuplicateUserException;
import com.example.exception.SpecialtyNotFoundException;
import com.example.repository.SpecialtyRepository;
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
class SpecialtyServiceTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private SpecialtyMapper mapper;

    @InjectMocks
    private SpecialtyService specialtyService;

    private Specialty specialty;
    private SpecialtyResponse specialtyResponse;

    @BeforeEach
    void setUp() {
        specialty = Specialty.builder()
                .id(1L)
                .name("Computer Science")
                .build();

        specialtyResponse = new SpecialtyResponse(1L, "Computer Science");
    }

    @Test
    void shouldGetSpecialtyByIdSuccessfully() {
        // Arrange
        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(specialty));
        when(mapper.toSpecialtyResponse(specialty)).thenReturn(specialtyResponse);

        // Act
        SpecialtyResponse result = specialtyService.getSpecialtyById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Computer Science", result.name());
        verify(specialtyRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowSpecialtyNotFoundExceptionWhenGetByIdFails() {
        // Arrange
        when(specialtyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SpecialtyNotFoundException.class, () -> specialtyService.getSpecialtyById(999L));
        verify(specialtyRepository, times(1)).findById(999L);
    }

    @Test
    void shouldGetSpecialtyByNameSuccessfully() {
        // Arrange
        when(specialtyRepository.findByName("Computer Science")).thenReturn(Optional.of(specialty));
        when(mapper.toSpecialtyResponse(specialty)).thenReturn(specialtyResponse);

        // Act
        SpecialtyResponse result = specialtyService.getSpecialtyByName("Computer Science");

        // Assert
        assertNotNull(result);
        assertEquals("Computer Science", result.name());
        verify(specialtyRepository, times(1)).findByName("Computer Science");
    }

    @Test
    void shouldThrowSpecialtyNotFoundExceptionWhenGetByNameFails() {
        // Arrange
        when(specialtyRepository.findByName("Unknown")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SpecialtyNotFoundException.class, () -> specialtyService.getSpecialtyByName("Unknown"));
        verify(specialtyRepository, times(1)).findByName("Unknown");
    }

    @Test
    void shouldGetAllSpecialtiesSuccessfully() {
        // Arrange
        List<Specialty> specialties = Arrays.asList(specialty);
        when(specialtyRepository.findAll()).thenReturn(specialties);
        when(mapper.toSpecialtyResponse(specialty)).thenReturn(specialtyResponse);

        // Act
        List<SpecialtyResponse> result = specialtyService.getAllSpecialties();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Computer Science", result.get(0).name());
        verify(specialtyRepository, times(1)).findAll();
    }

    @Test
    void shouldCreateSpecialtySuccessfully() {
        // Arrange
        CreateSpecialtyRequest request = new CreateSpecialtyRequest("Software Engineering");
        Specialty newSpecialty = Specialty.builder().name("Software Engineering").build();
        Specialty savedSpecialty = Specialty.builder().id(2L).name("Software Engineering").build();
        SpecialtyResponse newResponse = new SpecialtyResponse(2L, "Software Engineering");

        when(specialtyRepository.existsByName(request.name())).thenReturn(false);
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(savedSpecialty);
        when(mapper.toSpecialtyResponse(savedSpecialty)).thenReturn(newResponse);

        // Act
        SpecialtyResponse result = specialtyService.createSpecialty(request);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("Software Engineering", result.name());
        verify(specialtyRepository, times(1)).existsByName(request.name());
        verify(specialtyRepository, times(1)).save(any(Specialty.class));
    }

    @Test
    void shouldThrowDuplicateUserExceptionWhenCreatingExistingSpecialty() {
        // Arrange
        CreateSpecialtyRequest request = new CreateSpecialtyRequest("Computer Science");
        when(specialtyRepository.existsByName(request.name())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateUserException.class, () -> specialtyService.createSpecialty(request));
        verify(specialtyRepository, times(1)).existsByName(request.name());
        verify(specialtyRepository, never()).save(any(Specialty.class));
    }

    @Test
    void shouldUpdateSpecialtySuccessfully() {
        // Arrange
        UpdateSpecialtyRequest request = new UpdateSpecialtyRequest("Information Technology");
        Specialty updatedSpecialty = Specialty.builder().id(1L).name("Information Technology").build();
        SpecialtyResponse updatedResponse = new SpecialtyResponse(1L, "Information Technology");

        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(specialty));
        when(specialtyRepository.existsByName(request.name())).thenReturn(false);
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(updatedSpecialty);
        when(mapper.toSpecialtyResponse(updatedSpecialty)).thenReturn(updatedResponse);

        // Act
        SpecialtyResponse result = specialtyService.updateSpecialty(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals("Information Technology", result.name());
        verify(specialtyRepository, times(1)).findById(1L);
        verify(specialtyRepository, times(1)).save(any(Specialty.class));
    }

    @Test
    void shouldUpdateSpecialtySuccessfullyWhenNameIsUnchanged() {
        // Arrange
        UpdateSpecialtyRequest request = new UpdateSpecialtyRequest("Computer Science");

        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(specialty));
        // Ad dəyişmədiyi üçün existsByName çağırılmamalıdır (şərtə əsasən qısaqapanma olacaq)
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(specialty);
        when(mapper.toSpecialtyResponse(specialty)).thenReturn(specialtyResponse);

        // Act
        SpecialtyResponse result = specialtyService.updateSpecialty(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals("Computer Science", result.name());
        verify(specialtyRepository, never()).existsByName(anyString());
        verify(specialtyRepository, times(1)).save(any(Specialty.class));
    }

    @Test
    void shouldThrowSpecialtyNotFoundExceptionWhenUpdatingNonExistentSpecialty() {
        // Arrange
        UpdateSpecialtyRequest request = new UpdateSpecialtyRequest("Information Technology");
        when(specialtyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SpecialtyNotFoundException.class, () -> specialtyService.updateSpecialty(999L, request));
        verify(specialtyRepository, times(1)).findById(999L);
        verify(specialtyRepository, never()).save(any(Specialty.class));
    }

    @Test
    void shouldThrowDuplicateUserExceptionWhenUpdatingToExistingName() {
        // Arrange
        UpdateSpecialtyRequest request = new UpdateSpecialtyRequest("Existing Specialty");

        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(specialty));
        when(specialtyRepository.existsByName(request.name())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateUserException.class, () -> specialtyService.updateSpecialty(1L, request));
        verify(specialtyRepository, times(1)).existsByName(request.name());
        verify(specialtyRepository, never()).save(any(Specialty.class));
    }

    @Test
    void shouldDeleteSpecialtySuccessfully() {
        // Arrange
        when(specialtyRepository.existsById(1L)).thenReturn(true);

        // Act
        specialtyService.deleteSpecialty(1L);

        // Assert
        verify(specialtyRepository, times(1)).existsById(1L);
        verify(specialtyRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowSpecialtyNotFoundExceptionWhenDeletingNonExistentSpecialty() {
        // Arrange
        when(specialtyRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(SpecialtyNotFoundException.class, () -> specialtyService.deleteSpecialty(999L));
        verify(specialtyRepository, times(1)).existsById(999L);
        verify(specialtyRepository, never()).deleteById(anyLong());
    }
}