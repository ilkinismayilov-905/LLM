package com.example.service;

import com.example.dto.mapper.EntityToDtoMapper;
import com.example.dto.request.MarkAttendanceRequest;
import com.example.dto.response.AttendanceResponse;
import com.example.dto.response.AttendanceWarningResponse;
import com.example.entity.*;
import com.example.enums.AttendanceStatus;
import com.example.enums.Role;
import com.example.exception.*;
import com.example.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private UserRepository userRepository;

    // ...existing code...

    @Mock
    private EntityToDtoMapper mapper;

    @InjectMocks
    private AttendanceService attendanceService;

    private User teacherUser;
    private Teacher teacher;
    private Student student;
    private Group group;
    private Subject subject;
    private TeacherGroupSubject tgs;
    private Lesson lesson;
    private Attendance attendance;

    @BeforeEach
    void setUp() {
        teacherUser = User.builder()
                .id(1L)
                .email("teacher@school.com")
                .password("encoded")
                .firstName("John")
                .lastName("Doe")
                .role(Role.TEACHER)
                .isActive(true)
                .build();

        teacher = Teacher.builder()
                .id(1L)
                .user(teacherUser)
                .department("Mathematics")
                .build();

        group = Group.builder()
                .id(1L)
                .groupNumber("CS-101")
                .build();

        subject = Subject.builder()
                .id(1L)
                .name("Advanced Java")
                .credits(4)
                .build();

        tgs = TeacherGroupSubject.builder()
                .id(1L)
                .teacher(teacher)
                .group(group)
                .subject(subject)
                .build();

        User studentUser = User.builder()
                .id(2L)
                .email("student@school.com")
                .password("encoded")
                .firstName("Jane")
                .lastName("Smith")
                .role(Role.STUDENT)
                .isActive(true)
                .build();

        student = Student.builder()
                .id(1L)
                .user(studentUser)
                .studentNumber("STU001")
                .group(group)
                .build();

        LocalDateTime lessonStart = LocalDateTime.now();
        LocalDateTime lessonEnd = lessonStart.plusMinutes(80);

        lesson = Lesson.builder()
                .id(1L)
                .teacherGroupSubject(tgs)
                .startTime(lessonStart)
                .endTime(lessonEnd)
                .isActive(true)
                .build();

        attendance = Attendance.builder()
                .id(1L)
                .lesson(lesson)
                .student(student)
                .status(AttendanceStatus.ABSENT)
                .markedAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();
    }

    private void mockAuthenticatedTeacher() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("teacher@school.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("teacher@school.com")).thenReturn(Optional.of(teacherUser));
        when(teacherRepository.findByUserId(1L)).thenReturn(Optional.of(teacher));
    }

    @Test
    void shouldMarkAttendanceSuccessfully() {
        // Arrange
        mockAuthenticatedTeacher();
        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .lessonId(1L)
                .studentId(1L)
                .status("ABSENT")
                .remarks("Late arrival")
                .build();

        AttendanceResponse response = AttendanceResponse.builder()
                .id(1L)
                .status("ABSENT")
                .build();

        when(lessonRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(lesson));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(attendanceRepository.findByLessonAndStudent(lesson, student)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(mapper.toAttendanceResponse(any(Attendance.class))).thenReturn(response);

        // Act
        Object result = attendanceService.markAttendance(request);

        // Assert
        assertNotNull(result);
        assertInstanceOf(AttendanceResponse.class, result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void shouldThrowExceptionWhenLessonInactive() {
        Lesson inactiveLessonz = lesson;
        inactiveLessonz.setActive(false);

        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .lessonId(1L)
                .studentId(1L)
                .status("ABSENT")
                .build();

        when(lessonRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidInputException.class, () -> attendanceService.markAttendance(request));
    }

    @Test
    void shouldThrowExceptionWhenStudentNotFound() {
        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .lessonId(1L)
                .studentId(999L)
                .status("ABSENT")
                .build();

        when(lessonRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(lesson));
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class, () -> attendanceService.markAttendance(request));
    }

    @Test
    void shouldThrowExceptionWhenTeacherNotAssigned() {
        // Arrange
        mockAuthenticatedTeacher();
        Teacher otherTeacher = Teacher.builder()
                .id(999L)
                .user(teacherUser)
                .department("Other")
                .build();

        Lesson otherLesson = Lesson.builder()
                .id(1L)
                .teacherGroupSubject(TeacherGroupSubject.builder()
                        .teacher(otherTeacher)
                        .group(group)
                        .subject(subject)
                        .build())
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(80))
                .isActive(true)
                .build();

        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .lessonId(1L)
                .studentId(1L)
                .status("ABSENT")
                .build();

        when(lessonRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(otherLesson));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        // Act & Assert
        assertThrows(UnauthorizedTeacherException.class, () -> attendanceService.markAttendance(request));
    }

    @Test
    void shouldThrowExceptionWhenLessonNotStarted() {
        // Arrange
        mockAuthenticatedTeacher();
        LocalDateTime futureStart = LocalDateTime.now().plusHours(1);
        LocalDateTime futureEnd = futureStart.plusMinutes(80);

        Lesson futureLesson = Lesson.builder()
                .id(1L)
                .teacherGroupSubject(tgs)
                .startTime(futureStart)
                .endTime(futureEnd)
                .isActive(true)
                .build();

        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .lessonId(1L)
                .studentId(1L)
                .status("ABSENT")
                .build();

        when(lessonRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(futureLesson));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        // Act & Assert
        assertThrows(InvalidInputException.class, () -> attendanceService.markAttendance(request));
    }

    @Test
    void shouldThrowExceptionWhenLessonEnded() {
        // Arrange
        mockAuthenticatedTeacher();
        LocalDateTime pastStart = LocalDateTime.now().minusHours(2);
        LocalDateTime pastEnd = LocalDateTime.now().minusHours(1);

        Lesson pastLesson = Lesson.builder()
                .id(1L)
                .teacherGroupSubject(tgs)
                .startTime(pastStart)
                .endTime(pastEnd)
                .isActive(true)
                .build();

        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .lessonId(1L)
                .studentId(1L)
                .status("ABSENT")
                .build();

        when(lessonRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(pastLesson));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        // Act & Assert
        assertThrows(InvalidInputException.class, () -> attendanceService.markAttendance(request));
    }

    @Test
    void shouldAllowRevertAbsentToPresentWithin15Minutes() {
        // Arrange
        mockAuthenticatedTeacher();
        LocalDateTime lessonStart = LocalDateTime.now();
        LocalDateTime lessonEnd = lessonStart.plusMinutes(80);

        Lesson ongoingLesson = Lesson.builder()
                .id(1L)
                .teacherGroupSubject(tgs)
                .startTime(lessonStart)
                .endTime(lessonEnd)
                .isActive(true)
                .build();

        Attendance existingAttendance = Attendance.builder()
                .id(1L)
                .lesson(ongoingLesson)
                .student(student)
                .status(AttendanceStatus.ABSENT)
                .markedAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .lessonId(1L)
                .studentId(1L)
                .status("PRESENT")
                .build();

        AttendanceResponse response = AttendanceResponse.builder()
                .id(1L)
                .status("PRESENT")
                .build();

        when(lessonRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(ongoingLesson));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(attendanceRepository.findByLessonAndStudent(ongoingLesson, student)).thenReturn(Optional.of(existingAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(existingAttendance);
        when(mapper.toAttendanceResponse(any(Attendance.class))).thenReturn(response);

        // Act
        Object result = attendanceService.markAttendance(request);

        // Assert
        assertNotNull(result);
        assertInstanceOf(AttendanceResponse.class, result);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void shouldReturnWarningWhenRevertAbsentToPresentAfter15Minutes() {
        // Arrange
        mockAuthenticatedTeacher();
        LocalDateTime lessonStart = LocalDateTime.now().minusMinutes(20);
        LocalDateTime lessonEnd = lessonStart.plusMinutes(80);

        Lesson ongoingLesson = Lesson.builder()
                .id(1L)
                .teacherGroupSubject(tgs)
                .startTime(lessonStart)
                .endTime(lessonEnd)
                .isActive(true)
                .build();

        Attendance existingAttendance = Attendance.builder()
                .id(1L)
                .lesson(ongoingLesson)
                .student(student)
                .status(AttendanceStatus.ABSENT)
                .markedAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .lessonId(1L)
                .studentId(1L)
                .status("PRESENT")
                .build();

        when(lessonRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(ongoingLesson));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(attendanceRepository.findByLessonAndStudent(ongoingLesson, student)).thenReturn(Optional.of(existingAttendance));

        // Act
        Object result = attendanceService.markAttendance(request);

        // Assert
        assertNotNull(result);
        assertInstanceOf(AttendanceWarningResponse.class, result);
        AttendanceWarningResponse warning = (AttendanceWarningResponse) result;
        assertTrue(warning.warning());
        assertTrue(warning.message().contains("15 minutes"));
        assertEquals("ABSENT", warning.currentStatus());
        assertEquals("PRESENT", warning.requestedStatus());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void shouldGetStudentAttendanceSuccessfully() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(attendanceRepository.findByStudent(student)).thenReturn(java.util.List.of(attendance));
        when(mapper.toAttendanceResponse(attendance)).thenReturn(AttendanceResponse.builder()
                .id(1L)
                .status("ABSENT")
                .build());

        // Act
        java.util.List<AttendanceResponse> result = attendanceService.getStudentAttendance(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(attendanceRepository, times(1)).findByStudent(student);
    }

    @Test
    void shouldThrowExceptionWhenGettingAttendanceForNonExistentStudent() {
        // Arrange
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StudentNotFoundException.class, () -> attendanceService.getStudentAttendance(999L));
    }

    @Test
    void shouldGetLessonAttendanceSuccessfully() {
        // Arrange
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(attendanceRepository.findByLesson(lesson)).thenReturn(java.util.List.of(attendance));
        when(mapper.toAttendanceResponse(attendance)).thenReturn(AttendanceResponse.builder()
                .id(1L)
                .status("ABSENT")
                .build());

        // Act
        java.util.List<AttendanceResponse> result = attendanceService.getLessonAttendance(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(attendanceRepository, times(1)).findByLesson(lesson);
    }
}

