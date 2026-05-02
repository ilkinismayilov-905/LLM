package com.example.service;

import com.example.dto.mapper.StudentDashboardMapper;
import com.example.dto.response.EnrolledSubjectResponse;
import com.example.dto.response.StudentProfileResponse;
import com.example.dto.response.SubjectAcademicStatusResponse;
import com.example.entity.Grade;
import com.example.entity.Group;
import com.example.entity.Student;
import com.example.entity.StudentSubjectAbsence;
import com.example.entity.Subject;
import com.example.entity.User;
import com.example.exception.StudentNotFoundException;
import com.example.repository.GradeRepository;
import com.example.repository.StudentRepository;
import com.example.repository.StudentSubjectAbsenceRepository;
import com.example.repository.TeacherGroupSubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentDashboardServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private StudentSubjectAbsenceRepository studentSubjectAbsenceRepository;

    @Mock
    private TeacherGroupSubjectRepository teacherGroupSubjectRepository;

    @Mock
    private StudentDashboardMapper mapper;

    @InjectMocks
    private StudentDashboardService studentDashboardService;

    private User user;
    private Group group;
    private Student student;
    private Subject subject;
    private Grade grade;
    private StudentSubjectAbsence absence;
    private StudentProfileResponse profileResponse;
    private EnrolledSubjectResponse enrolledSubjectResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("student@school.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        group = Group.builder()
                .id(10L)
                .groupNumber("CS-101")
                .build();

        student = Student.builder()
                .id(100L)
                .user(user)
                .studentNumber("STU001")
                .group(group)
                .build();

        subject = Subject.builder()
                .id(200L)
                .name("Advanced Java")
                .credits(4)
                .build();

        grade = Grade.builder()
                .id(300L)
                .student(student)
                .subject(subject)
                .totalScore(85)
                .build();

        absence = StudentSubjectAbsence.builder()
                .id(400L)
                .student(student)
                .subject(subject)
                .absenceCount(3)
                .build();

        profileResponse = StudentProfileResponse.builder()
                .studentNumber("STU001")
                .firstName("John")
                .lastName("Doe")
                .email("student@school.com")
                .groupNumber("CS-101")
                .build();

        enrolledSubjectResponse = EnrolledSubjectResponse.builder()
                .id(200L)
                .name("Advanced Java")
                .credits(4)
                .build();
    }

    @Test
    void shouldGetStudentProfileSuccessfully() {
        // Arrange
        when(studentRepository.findByUserIdWithDetails(1L)).thenReturn(Optional.of(student));
        when(mapper.toStudentProfileResponse(student)).thenReturn(profileResponse);

        // Act
        StudentProfileResponse result = studentDashboardService.getStudentProfile(1L);

        // Assert
        assertNotNull(result);
        assertEquals("STU001", result.studentNumber());
        assertEquals("John", result.firstName());
        verify(studentRepository, times(1)).findByUserIdWithDetails(1L);
        verify(mapper, times(1)).toStudentProfileResponse(student);
    }

    @Test
    void shouldThrowStudentNotFoundExceptionWhenProfileNotFound() {
        // Arrange
        when(studentRepository.findByUserIdWithDetails(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StudentNotFoundException.class, () -> studentDashboardService.getStudentProfile(999L));
        verify(studentRepository, times(1)).findByUserIdWithDetails(999L);
        verify(mapper, never()).toStudentProfileResponse(any());
    }

    @Test
    void shouldGetStudentAcademicStatusSuccessfully() {
        // Arrange
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(student));
        when(gradeRepository.findAllByStudentIdWithSubjectDetails(100L)).thenReturn(Collections.singletonList(grade));
        when(studentSubjectAbsenceRepository.findByStudentAndSubject(student, subject)).thenReturn(Optional.of(absence));

        // Act
        List<SubjectAcademicStatusResponse> result = studentDashboardService.getStudentAcademicStatus(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        SubjectAcademicStatusResponse status = result.get(0);
        assertEquals(200L, status.subjectId());
        assertEquals("Advanced Java", status.subjectName());
        assertEquals(4, status.credits());
        assertEquals(1, status.grades().size());
        assertEquals(85, status.grades().get(0));
        assertEquals(3, status.totalAbsences());

        verify(studentRepository, times(1)).findByUserId(1L);
        verify(gradeRepository, times(1)).findAllByStudentIdWithSubjectDetails(100L);
        verify(studentSubjectAbsenceRepository, times(1)).findByStudentAndSubject(student, subject);
    }

    @Test
    void shouldGetStudentAcademicStatusWithZeroAbsencesWhenNoAbsenceRecordExists() {
        // Arrange
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(student));
        when(gradeRepository.findAllByStudentIdWithSubjectDetails(100L)).thenReturn(Collections.singletonList(grade));
        when(studentSubjectAbsenceRepository.findByStudentAndSubject(student, subject)).thenReturn(Optional.empty());

        // Act
        List<SubjectAcademicStatusResponse> result = studentDashboardService.getStudentAcademicStatus(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).totalAbsences()); // Absence count should default to 0
    }

    @Test
    void shouldThrowStudentNotFoundExceptionWhenGettingAcademicStatusForUnknownUser() {
        // Arrange
        when(studentRepository.findByUserId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StudentNotFoundException.class, () -> studentDashboardService.getStudentAcademicStatus(999L));
        verify(studentRepository, times(1)).findByUserId(999L);
        verify(gradeRepository, never()).findAllByStudentIdWithSubjectDetails(anyLong());
    }

    @Test
    void shouldGetStudentEnrolledSubjectsSuccessfully() {
        // Arrange
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(student));
        when(teacherGroupSubjectRepository.findDistinctSubjectsByGroupIdWithDetails(10L))
                .thenReturn(Collections.singletonList(subject));
        when(mapper.toEnrolledSubjectResponse(subject)).thenReturn(enrolledSubjectResponse);

        // Act
        List<EnrolledSubjectResponse> result = studentDashboardService.getStudentEnrolledSubjects(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Advanced Java", result.get(0).name());
        verify(studentRepository, times(1)).findByUserId(1L);
        verify(teacherGroupSubjectRepository, times(1)).findDistinctSubjectsByGroupIdWithDetails(10L);
        verify(mapper, times(1)).toEnrolledSubjectResponse(subject);
    }

    @Test
    void shouldThrowStudentNotFoundExceptionWhenGettingEnrolledSubjectsForUnknownUser() {
        // Arrange
        when(studentRepository.findByUserId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StudentNotFoundException.class, () -> studentDashboardService.getStudentEnrolledSubjects(999L));
        verify(studentRepository, times(1)).findByUserId(999L);
        verify(teacherGroupSubjectRepository, never()).findDistinctSubjectsByGroupIdWithDetails(anyLong());
    }
}