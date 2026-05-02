package com.example.service;

import com.example.dto.DashboardSummaryDTO;
import com.example.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuperAdminDashboardServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private SuperAdminDashboardService superAdminDashboardService;

    @Test
    void shouldGetDashboardSummarySuccessfully() {
        // Arrange
        long expectedTotalStudents = 1200L;
        long expectedActiveStudents = 1150L;
        long expectedTotalTeachers = 85L;
        long expectedTotalGroups = 40L;
        long expectedTotalDepartments = 8L;
        long expectedTotalSpecialties = 15L;
        long expectedTotalUsers = 1300L;
        long expectedTotalSubjects = 45L;

        when(studentRepository.countTotalStudents()).thenReturn(expectedTotalStudents);
        when(studentRepository.countActiveStudents()).thenReturn(expectedActiveStudents);
        when(teacherRepository.countTotalTeachers()).thenReturn(expectedTotalTeachers);
        when(groupRepository.count()).thenReturn(expectedTotalGroups);
        when(teacherRepository.countDistinctDepartments()).thenReturn(expectedTotalDepartments);
        when(specialtyRepository.count()).thenReturn(expectedTotalSpecialties);
        when(userRepository.count()).thenReturn(expectedTotalUsers);
        when(subjectRepository.count()).thenReturn(expectedTotalSubjects);

        // Act
        DashboardSummaryDTO result = superAdminDashboardService.getDashboardSummary();

        // Assert
        assertNotNull(result);
        assertEquals(expectedTotalStudents, result.totalStudents());
        assertEquals(expectedActiveStudents, result.activeStudents());
        assertEquals(expectedTotalTeachers, result.totalTeachers());
        assertEquals(expectedTotalGroups, result.totalGroups());
        assertEquals(expectedTotalDepartments, result.totalDepartments());
        assertEquals(expectedTotalSpecialties, result.totalSpecialties());
        assertEquals(expectedTotalUsers, result.totalUsers());
        assertEquals(expectedTotalSubjects, result.totalSubjects());

        // Verify that all repository methods were called exactly once
        verify(studentRepository, times(1)).countTotalStudents();
        verify(studentRepository, times(1)).countActiveStudents();
        verify(teacherRepository, times(1)).countTotalTeachers();
        verify(groupRepository, times(1)).count();
        verify(teacherRepository, times(1)).countDistinctDepartments();
        verify(specialtyRepository, times(1)).count();
        verify(userRepository, times(1)).count();
        verify(subjectRepository, times(1)).count();
    }
}