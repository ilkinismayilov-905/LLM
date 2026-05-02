package com.example.service;

import com.example.entity.Student;
import com.example.entity.StudentSubjectAbsence;
import com.example.entity.Subject;
import com.example.repository.AttendanceRepository;
import com.example.repository.StudentSubjectAbsenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceTrackingServiceTest {

    @Mock
    private StudentSubjectAbsenceRepository studentSubjectAbsenceRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceTrackingService attendanceTrackingService;

    private Student student;
    private Subject subject;
    private StudentSubjectAbsence studentSubjectAbsence;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .id(1L)
                .studentNumber("STU001")
                .build();

        subject = Subject.builder()
                .id(1L)
                .name("Advanced Java")
                .absenceLimit(3) // Fərz edirik ki, limit 3-dür
                .build();

        studentSubjectAbsence = StudentSubjectAbsence.builder()
                .id(1L)
                .student(student)
                .subject(subject)
                .absenceCount(2) // Limiti keçməmək üçün cari sayı 2 təyin edirik
                .failedDueToAbsence(false)
                .build();
    }

    @Test
    void shouldTrackAbsenceSuccessfullyForNewRecord() {
        // Arrange
        when(studentSubjectAbsenceRepository.findByStudentAndSubject(student, subject))
                .thenReturn(Optional.empty());

        when(studentSubjectAbsenceRepository.save(any(StudentSubjectAbsence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StudentSubjectAbsence result = attendanceTrackingService.trackAbsence(student, subject);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getAbsenceCount());
        assertFalse(result.getFailedDueToAbsence());
        verify(studentSubjectAbsenceRepository, times(1)).findByStudentAndSubject(student, subject);
        verify(studentSubjectAbsenceRepository, times(1)).save(any(StudentSubjectAbsence.class));
    }

    @Test
    void shouldTrackAbsenceAndFailStudentWhenLimitExceeded() {
        // Arrange
        studentSubjectAbsence.setAbsenceCount(3); // Artıq limitdədir, +1 olanda fail olacaq

        when(studentSubjectAbsenceRepository.findByStudentAndSubject(student, subject))
                .thenReturn(Optional.of(studentSubjectAbsence));

        when(studentSubjectAbsenceRepository.save(any(StudentSubjectAbsence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StudentSubjectAbsence result = attendanceTrackingService.trackAbsence(student, subject);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getAbsenceCount());
        assertTrue(result.getFailedDueToAbsence()); // AbsenceLimitStrategy-nin fail verdiyini fərz edirik
        verify(studentSubjectAbsenceRepository, times(1)).save(studentSubjectAbsence);
    }

    @Test
    void shouldReturnTrueWhenStudentHasFailed() {
        // Arrange
        studentSubjectAbsence.setFailedDueToAbsence(true);
        when(studentSubjectAbsenceRepository.findByStudentAndSubject(student, subject))
                .thenReturn(Optional.of(studentSubjectAbsence));

        // Act
        boolean result = attendanceTrackingService.hasStudentFailedDueToAbsence(student, subject);

        // Assert
        assertTrue(result);
        verify(studentSubjectAbsenceRepository, times(1)).findByStudentAndSubject(student, subject);
    }

    @Test
    void shouldReturnFalseWhenStudentHasNotFailedOrRecordDoesNotExist() {
        // Arrange
        when(studentSubjectAbsenceRepository.findByStudentAndSubject(student, subject))
                .thenReturn(Optional.empty());

        // Act
        boolean result = attendanceTrackingService.hasStudentFailedDueToAbsence(student, subject);

        // Assert
        assertFalse(result);
        verify(studentSubjectAbsenceRepository, times(1)).findByStudentAndSubject(student, subject);
    }

    @Test
    void shouldGetAbsenceCountSuccessfully() {
        // Arrange
        when(studentSubjectAbsenceRepository.findByStudentAndSubject(student, subject))
                .thenReturn(Optional.of(studentSubjectAbsence));

        // Act
        int result = attendanceTrackingService.getAbsenceCount(student, subject);

        // Assert
        assertEquals(2, result);
        verify(studentSubjectAbsenceRepository, times(1)).findByStudentAndSubject(student, subject);
    }

    @Test
    void shouldReturnZeroAbsenceCountWhenRecordDoesNotExist() {
        // Arrange
        when(studentSubjectAbsenceRepository.findByStudentAndSubject(student, subject))
                .thenReturn(Optional.empty());

        // Act
        int result = attendanceTrackingService.getAbsenceCount(student, subject);

        // Assert
        assertEquals(0, result);
        verify(studentSubjectAbsenceRepository, times(1)).findByStudentAndSubject(student, subject);
    }

    @Test
    void shouldReturnExistingRecordWhenGetOrCreate() {
        // Arrange
        when(studentSubjectAbsenceRepository.findByStudentAndSubject(student, subject))
                .thenReturn(Optional.of(studentSubjectAbsence));

        // Act
        StudentSubjectAbsence result = attendanceTrackingService.getOrCreateStudentSubjectAbsence(student, subject);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getAbsenceCount());
        verify(studentSubjectAbsenceRepository, times(1)).findByStudentAndSubject(student, subject);
        verify(studentSubjectAbsenceRepository, never()).save(any(StudentSubjectAbsence.class));
    }

    @Test
    void shouldCreateNewRecordWhenGetOrCreateNotFound() {
        // Arrange
        when(studentSubjectAbsenceRepository.findByStudentAndSubject(student, subject))
                .thenReturn(Optional.empty());

        when(studentSubjectAbsenceRepository.save(any(StudentSubjectAbsence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StudentSubjectAbsence result = attendanceTrackingService.getOrCreateStudentSubjectAbsence(student, subject);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getAbsenceCount());
        assertFalse(result.getFailedDueToAbsence());
        verify(studentSubjectAbsenceRepository, times(1)).findByStudentAndSubject(student, subject);
        verify(studentSubjectAbsenceRepository, times(1)).save(any(StudentSubjectAbsence.class));
    }
}