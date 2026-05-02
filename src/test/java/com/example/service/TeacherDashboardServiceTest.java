package com.example.service;

import com.example.dto.response.GroupDto;
import com.example.dto.response.SubjectDto;
import com.example.dto.response.TeacherDashboardResponse;
import com.example.entity.Group;
import com.example.entity.Subject;
import com.example.entity.Teacher;
import com.example.entity.TeacherGroupSubject;
import com.example.entity.User;
import com.example.enums.Department;
import com.example.enums.Role;
import com.example.exception.TeacherNotFoundException;
import com.example.exception.UnauthorizedTeacherException;
import com.example.repository.TeacherGroupSubjectRepository;
import com.example.repository.TeacherRepository;
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
class TeacherDashboardServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private TeacherGroupSubjectRepository teacherGroupSubjectRepository;

    @InjectMocks
    private TeacherDashboardService teacherDashboardService;

    private User teacherUser;
    private Teacher teacher;
    private Group group1;
    private Group group2;
    private Subject subject1;
    private Subject subject2;
    private TeacherGroupSubject assignment1;
    private TeacherGroupSubject assignment2;
    private TeacherGroupSubject assignment3;

    @BeforeEach
    void setUp() {
        teacherUser = User.builder()
                .id(1L)
                .email("teacher@school.com")
                .firstName("Jane")
                .lastName("Smith")
                .role(Role.TEACHER)
                .build();

        teacher = Teacher.builder()
                .id(100L)
                .user(teacherUser)
                .department(Department.COMPUTER_SCIENCE)
                .build();

        group1 = Group.builder().id(10L).groupNumber("CS-101").build();
        group2 = Group.builder().id(20L).groupNumber("CS-102").build();

        subject1 = Subject.builder().id(1L).name("Java").credits(4).build();
        subject2 = Subject.builder().id(2L).name("Database").credits(5).build();

        // 1-ci təyinat: Qrup 1 və Fənn 1
        assignment1 = TeacherGroupSubject.builder()
                .id(1L)
                .teacher(teacher)
                .group(group1)
                .subject(subject1)
                .build();

        // 2-ci təyinat: Qrup 1 və Fənn 2 (Qrup 1 təkrarlanır, fənn fərqlidir)
        assignment2 = TeacherGroupSubject.builder()
                .id(2L)
                .teacher(teacher)
                .group(group1)
                .subject(subject2)
                .build();

        // 3-cü təyinat: Qrup 2 və Fənn 1 (Fənn 1 təkrarlanır, qrup fərqlidir)
        assignment3 = TeacherGroupSubject.builder()
                .id(3L)
                .teacher(teacher)
                .group(group2)
                .subject(subject1)
                .build();
    }

    @Test
    void shouldGetTeacherDashboardDataSuccessfully() {
        // Arrange
        when(teacherRepository.findByUserIdWithDetails(1L)).thenReturn(Optional.of(teacher));
        // Biz qəsdən təkrarlanan qrup və fənləri ehtiva edən siyahı qaytarırıq ki, "distinct" məntiqini test edək
        when(teacherGroupSubjectRepository.findAllByTeacherId(100L))
                .thenReturn(Arrays.asList(assignment1, assignment2, assignment3));

        // Act
        TeacherDashboardResponse result = teacherDashboardService.getTeacherDashboardData(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.firstName());
        assertEquals("Smith", result.lastName());
        assertEquals("teacher@school.com", result.email());
        assertEquals("COMPUTER_SCIENCE", result.department());

        // Qruplar təkrarlanmamalıdır (distinct işləməlidir)
        List<GroupDto> assignedGroups = result.assignedGroups();
        assertNotNull(assignedGroups);
        assertEquals(2, assignedGroups.size());
        assertTrue(assignedGroups.stream().anyMatch(g -> g.groupNumber().equals("CS-101")));
        assertTrue(assignedGroups.stream().anyMatch(g -> g.groupNumber().equals("CS-102")));

        // Fənlər təkrarlanmamalıdır (distinct işləməlidir)
        List<SubjectDto> assignedSubjects = result.assignedSubjects();
        assertNotNull(assignedSubjects);
        assertEquals(2, assignedSubjects.size());
        assertTrue(assignedSubjects.stream().anyMatch(s -> s.name().equals("Java")));
        assertTrue(assignedSubjects.stream().anyMatch(s -> s.name().equals("Database")));

        verify(teacherRepository, times(1)).findByUserIdWithDetails(1L);
        verify(teacherGroupSubjectRepository, times(1)).findAllByTeacherId(100L);
    }

    @Test
    void shouldGetTeacherDashboardDataWithNoAssignments() {
        // Arrange
        when(teacherRepository.findByUserIdWithDetails(1L)).thenReturn(Optional.of(teacher));
        when(teacherGroupSubjectRepository.findAllByTeacherId(100L)).thenReturn(Collections.emptyList());

        // Act
        TeacherDashboardResponse result = teacherDashboardService.getTeacherDashboardData(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.assignedGroups().isEmpty());
        assertTrue(result.assignedSubjects().isEmpty());

        verify(teacherRepository, times(1)).findByUserIdWithDetails(1L);
        verify(teacherGroupSubjectRepository, times(1)).findAllByTeacherId(100L);
    }

    @Test
    void shouldThrowTeacherNotFoundExceptionWhenTeacherDoesNotExist() {
        // Arrange
        when(teacherRepository.findByUserIdWithDetails(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TeacherNotFoundException.class, () -> teacherDashboardService.getTeacherDashboardData(999L));

        verify(teacherRepository, times(1)).findByUserIdWithDetails(999L);
        verify(teacherGroupSubjectRepository, never()).findAllByTeacherId(anyLong());
    }

    @Test
    void shouldThrowUnauthorizedTeacherExceptionWhenRoleIsNotTeacher() {
        // Arrange
        User adminUser = User.builder()
                .id(2L)
                .role(Role.SUPER_ADMIN) // Rol teacher deyil
                .build();

        Teacher unauthorizedTeacher = Teacher.builder()
                .id(101L)
                .user(adminUser)
                .build();

        when(teacherRepository.findByUserIdWithDetails(2L)).thenReturn(Optional.of(unauthorizedTeacher));

        // Act & Assert
        assertThrows(UnauthorizedTeacherException.class, () -> teacherDashboardService.getTeacherDashboardData(2L));

        verify(teacherRepository, times(1)).findByUserIdWithDetails(2L);
        verify(teacherGroupSubjectRepository, never()).findAllByTeacherId(anyLong());
    }
}