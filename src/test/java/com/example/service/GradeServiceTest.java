package com.example.service;

import com.example.dto.mapper.GradeMapper;
import com.example.dto.request.CreateGradeRequest;
import com.example.dto.request.UpdateGradeRequest;
import com.example.dto.response.*;
import com.example.entity.*;
import com.example.enums.Department;
import com.example.enums.GradeStatus;
import com.example.enums.Role;
import com.example.exception.*;
import com.example.repository.*;
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
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private GradeMapper mapper;

    @Mock
    private AttendanceTrackingService attendanceTrackingService;

    @InjectMocks
    private GradeService gradeService;

    private Grade grade;
    private Student student;
    private Subject subject;
    private Teacher teacher;
    private GradeResponse gradeResponse;

    @BeforeEach
    void setUp() {
        User studentUser = User.builder()
                .id(1L)
                .email("student@school.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.STUDENT)
                .build();

        student = Student.builder()
                .id(1L)
                .user(studentUser)
                .studentNumber("STU001")
                .build();

        subject = Subject.builder()
                .id(1L)
                .name("Advanced Java")
                .credits(4)
                .build();

        User teacherUser = User.builder()
                .id(2L)
                .email("teacher@school.com")
                .firstName("Jane")
                .lastName("Smith")
                .role(Role.TEACHER)
                .build();

        teacher = Teacher.builder()
                .id(1L)
                .user(teacherUser)
                .department(Department.COMPUTER_SCIENCE)
                .build();

        grade = Grade.builder()
                .id(1L)
                .student(student)
                .subject(subject)
                .teacher(teacher)
                .attendanceScore(8)
                .seminarScore(7)
                .col1(6)
                .col2(7)
                .col3(8)
                .examScore(35)
                .totalScore(71)
                .status(GradeStatus.PASSED)
                .build();

        gradeResponse = GradeResponse.builder()
                .id(1L)
                .attendanceScore(8)
                .seminarScore(7)
                .col1(6)
                .col2(7)
                .col3(8)
                .examScore(35)
                .totalScore(71)
                .status("PASSED")
                .build();

        lenient().when(attendanceTrackingService.hasStudentFailedDueToAbsence(any(Student.class), any(Subject.class)))
                .thenReturn(false);
    }

    @Test
    void shouldGetGradeByIdSuccessfully() {
        // Arrange
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(mapper.toGradeResponse(grade)).thenReturn(gradeResponse);

        // Act
        GradeResponse result = gradeService.getGradeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(71, result.totalScore());
        verify(gradeRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowGradeNotFoundExceptionWhenGradeDoesNotExist() {
        // Arrange
        when(gradeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(GradeNotFoundException.class, () -> gradeService.getGradeById(999L));
    }

    @Test
    void shouldGetGradesByStudentIdSuccessfully() {
        // Arrange
        List<Grade> grades = Arrays.asList(grade);
        when(gradeRepository.findAllByStudentId(1L)).thenReturn(grades);
        when(mapper.toGradeResponse(grade)).thenReturn(gradeResponse);

        // Act
        List<GradeResponse> result = gradeService.getGradesByStudentId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gradeRepository, times(1)).findAllByStudentId(1L);
    }

    @Test
    void shouldGetGradesByTeacherIdSuccessfully() {
        // Arrange
        List<Grade> grades = Arrays.asList(grade);
        when(gradeRepository.findAllByTeacherId(1L)).thenReturn(grades);
        when(mapper.toGradeResponse(grade)).thenReturn(gradeResponse);

        // Act
        List<GradeResponse> result = gradeService.getGradesByTeacherId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gradeRepository, times(1)).findAllByTeacherId(1L);
    }

    @Test
    void shouldGetAllGradesSuccessfully() {
        // Arrange
        List<Grade> grades = Arrays.asList(grade);
        when(gradeRepository.findAll()).thenReturn(grades);
        when(mapper.toGradeResponse(grade)).thenReturn(gradeResponse);

        // Act
        List<GradeResponse> result = gradeService.getAllGrades();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gradeRepository, times(1)).findAll();
    }

    @Test
    void shouldCreateGradeSuccessfullyWithPassedStatus() {
        // Arrange
        CreateGradeRequest request = CreateGradeRequest.builder()
                .studentId(1L)
                .subjectId(1L)
                .teacherId(1L)
                .attendanceScore(8)
                .seminarScore(7)
                .col1(6)
                .col2(7)
                .col3(8)
                .examScore(35) // Total > 51, Exam > 17
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        when(gradeRepository.save(any(Grade.class))).thenAnswer(i -> {
            Grade saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        GradeResponse expectedResponse = GradeResponse.builder()
                .totalScore(71)
                .status("PASSED")
                .build();
        when(mapper.toGradeResponse(any(Grade.class))).thenReturn(expectedResponse);

        // Act
        GradeResponse result = gradeService.createGrade(request);

        // Assert
        assertNotNull(result);
        assertEquals(71, result.totalScore());
        assertEquals("PASSED", result.status());
        verify(gradeRepository, times(1)).save(any(Grade.class));
    }

    @Test
    void shouldCreateGradeWithFailedByTotalStatus() {
        // Arrange
        CreateGradeRequest request = CreateGradeRequest.builder()
                .studentId(1L)
                .subjectId(1L)
                .teacherId(1L)
                .attendanceScore(4)
                .seminarScore(4)
                .col1(4)
                .col2(4)
                .col3(4)
                .examScore(30) // Total = 50
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        when(gradeRepository.save(any(Grade.class))).thenAnswer(i -> i.getArgument(0));

        GradeResponse expectedResponse = GradeResponse.builder()
                .totalScore(50)
                .status("FAILED_BY_TOTAL")
                .build();
        when(mapper.toGradeResponse(any(Grade.class))).thenReturn(expectedResponse);

        // Act
        GradeResponse result = gradeService.createGrade(request);

        // Assert
        assertNotNull(result);
        assertEquals("FAILED_BY_TOTAL", result.status());
    }

    @Test
    void shouldCreateGradeWithFailedByExamStatus() {
        // Arrange
        CreateGradeRequest request = CreateGradeRequest.builder()
                .studentId(1L)
                .subjectId(1L)
                .teacherId(1L)
                .attendanceScore(10)
                .seminarScore(10)
                .col1(10)
                .col2(10)
                .col3(10)
                .examScore(15) // Total = 65, Exam = 15 (< 17)
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        when(gradeRepository.save(any(Grade.class))).thenAnswer(i -> i.getArgument(0));

        GradeResponse expectedResponse = GradeResponse.builder()
                .totalScore(65)
                .status("FAILED_BY_EXAM")
                .build();
        when(mapper.toGradeResponse(any(Grade.class))).thenReturn(expectedResponse);

        // Act
        GradeResponse result = gradeService.createGrade(request);

        // Assert
        assertNotNull(result);
        assertEquals("FAILED_BY_EXAM", result.status());
    }

    @Test
    void shouldThrowStudentFailedDueToAbsenceExceptionWhenCreatingGrade() {
        // Arrange
        CreateGradeRequest request = CreateGradeRequest.builder()
                .studentId(1L)
                .subjectId(1L)
                .teacherId(1L)
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        when(attendanceTrackingService.hasStudentFailedDueToAbsence(student, subject)).thenReturn(true);

        // Act & Assert
        assertThrows(StudentFailedDueToAbsenceException.class, () -> gradeService.createGrade(request));
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void shouldThrowInvalidGradeValueExceptionForCreateGrade() {
        // Arrange
        CreateGradeRequest request = CreateGradeRequest.builder()
                .studentId(1L)
                .subjectId(1L)
                .teacherId(1L)
                .attendanceScore(11) // Invalid
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        // Act & Assert
        assertThrows(InvalidGradeValueException.class, () -> gradeService.createGrade(request));
    }

    @Test
    void shouldUpdateGradeSuccessfully() {
        // Arrange
        UpdateGradeRequest request = UpdateGradeRequest.builder()
                .attendanceScore(9)
                .examScore(40)
                .build();

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(i -> i.getArgument(0));

        GradeResponse updatedResponse = GradeResponse.builder()
                .totalScore(77) // previous 71 - 8 (old att) + 9 (new att) - 35 (old exam) + 40 (new exam) = 77
                .status("PASSED")
                .build();
        when(mapper.toGradeResponse(any(Grade.class))).thenReturn(updatedResponse);

        // Act
        GradeResponse result = gradeService.updateGrade(1L, request);

        // Assert
        assertNotNull(result);
        verify(gradeRepository, times(1)).save(any(Grade.class));
    }

    @Test
    void shouldThrowInvalidGradeValueExceptionForUpdateGrade() {
        // Arrange
        UpdateGradeRequest request = UpdateGradeRequest.builder()
                .examScore(55) // Invalid > 50
                .build();

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));

        // Act & Assert
        assertThrows(InvalidGradeValueException.class, () -> gradeService.updateGrade(1L, request));
    }

    @Test
    void shouldThrowStudentFailedDueToAbsenceExceptionWhenUpdatingGrade() {
        // Arrange
        UpdateGradeRequest request = UpdateGradeRequest.builder().build();

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(attendanceTrackingService.hasStudentFailedDueToAbsence(grade.getStudent(), grade.getSubject())).thenReturn(true);

        // Act & Assert
        assertThrows(StudentFailedDueToAbsenceException.class, () -> gradeService.updateGrade(1L, request));
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void shouldDeleteGradeSuccessfully() {
        // Arrange
        when(gradeRepository.existsById(1L)).thenReturn(true);

        // Act
        gradeService.deleteGrade(1L);

        // Assert
        verify(gradeRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowGradeNotFoundExceptionWhenDeletingNonExistentGrade() {
        // Arrange
        when(gradeRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(GradeNotFoundException.class, () -> gradeService.deleteGrade(999L));
    }
}