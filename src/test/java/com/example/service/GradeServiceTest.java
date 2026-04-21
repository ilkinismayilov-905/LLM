package com.example.service;

import com.example.dto.mapper.EntityToDtoMapper;
import com.example.dto.request.CreateGradeRequest;
import com.example.dto.request.UpdateGradeRequest;
import com.example.dto.response.*;
import com.example.entity.*;
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
    private EntityToDtoMapper mapper;

    @InjectMocks
    private GradeService gradeService;

    private Grade grade;
    private Student student;
    private Subject subject;
    private Teacher teacher;
    private SpecialtyResponse specialtyRes;
    private GradeResponse gradeResponse;
    private UserResponse studentUserRes;
    private UserResponse teacherUserRes;
    private GroupResponse groupRes;
    private StudentResponse studentRes;
    private TeacherResponse teacherRes;
    private SubjectResponse subjectRes;




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
                .department("Computer Science")
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

        specialtyRes = new SpecialtyResponse(1L, "Software Engineering");

        GroupResponse groupRes = GroupResponse.builder()
                .id(1L)
                .groupNumber("601.21")
                .specialty(specialtyRes)
                .build();

        studentUserRes = UserResponse.builder()
                .id(1L)
                .email("student@school.com")
                .firstName("John")
                .lastName("Doe")
                .role("ROLE_STUDENT")
                .isActive(true)
                .build();

        studentRes = StudentResponse.builder()
                .id(1L)
                .studentNumber("STU001")
                .user(studentUserRes)
                .group(groupRes)
                .build();

        teacherUserRes = UserResponse.builder()
                .id(2L)
                .email("teacher@school.com")
                .firstName("Jane")
                .lastName("Smith")
                .role("ROLE_TEACHER")
                .isActive(true)
                .build();


        teacherRes = TeacherResponse.builder()
                .id(1L)
                .user(teacherUserRes)
                .department("Computer Science")
                .build();

        subjectRes = SubjectResponse.builder()
                .id(1L)
                .name("Advanced Java")
                .credits(4)
                .build();

        gradeResponse = GradeResponse.builder()
                .id(1L)
                .student(studentRes)   // Artıq Long yox, tam obyekt
                .subject(subjectRes)   // Artıq subjectId yox, subject obyekti
                .teacher(teacherRes)   // Artıq teacherId yox, teacher obyekti
                .attendanceScore(8)
                .seminarScore(7)
                .col1(6)
                .col2(7)
                .col3(8)
                .examScore(35)
                .totalScore(71)
                .status("PASSED")
                .build();
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
        List<GradeResponse> responses = Arrays.asList(gradeResponse);
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
        List<GradeResponse> responses = Arrays.asList(gradeResponse);
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
    void shouldCreateGradeWithExcellentStatusSuccessfully() {
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
                .examScore(35)
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(gradeRepository.save(any(Grade.class))).thenReturn(grade);
        when(mapper.toGradeResponse(grade)).thenReturn(gradeResponse);

        // Act
        GradeResponse result = gradeService.createGrade(request);

        // Assert
        assertNotNull(result);
        assertEquals(71, result.totalScore());
        assertEquals("PASSED", result.status());
        verify(gradeRepository, times(1)).save(any(Grade.class));
    }

    @Test
    void shouldCreateGradeWithGoodStatusSuccessfully() {
        // Arrange
        CreateGradeRequest request = CreateGradeRequest.builder()
                .studentId(1L)
                .subjectId(1L)
                .teacherId(1L)
                .attendanceScore(6)
                .seminarScore(5)
                .col1(5)
                .col2(5)
                .col3(5)
                .examScore(20)
                .build();

        Grade goodGrade = Grade.builder()
                .id(1L)
                .student(student)
                .subject(subject)
                .teacher(teacher)
                .attendanceScore(6)
                .seminarScore(5)
                .col1(5)
                .col2(5)
                .col3(5)
                .examScore(20)
                .totalScore(51)
                .status(GradeStatus.PASSED)
                .build();

        GradeResponse goodResponse = GradeResponse.builder()
                .totalScore(51)
                .status("GOOD")
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(gradeRepository.save(any(Grade.class))).thenReturn(goodGrade);
        when(mapper.toGradeResponse(goodGrade)).thenReturn(goodResponse);

        // Act
        GradeResponse result = gradeService.createGrade(request);

        // Assert
        assertNotNull(result);
        assertEquals(51, result.totalScore());
        assertEquals("GOOD", result.status());
    }

    @Test
    void shouldCreateGradeWithSatisfactoryStatusSuccessfully() {
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
                .examScore(16)
                .build();

        Grade satisfactoryGrade = Grade.builder()
                .id(1L)
                .student(student)
                .subject(subject)
                .teacher(teacher)
                .attendanceScore(4)
                .seminarScore(4)
                .col1(4)
                .col2(4)
                .col3(4)
                .examScore(16)
                .totalScore(40)
                .status(GradeStatus.PASSED)
                .build();

        GradeResponse satisfactoryResponse = GradeResponse.builder()
                .totalScore(40)
                .status("SATISFACTORY")
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(gradeRepository.save(any(Grade.class))).thenReturn(satisfactoryGrade);
        when(mapper.toGradeResponse(satisfactoryGrade)).thenReturn(satisfactoryResponse);

        // Act
        GradeResponse result = gradeService.createGrade(request);

        // Assert
        assertNotNull(result);
        assertEquals(40, result.totalScore());
        assertEquals("SATISFACTORY", result.status());
    }

    @Test
    void shouldCreateGradeWithFailStatusWhenTotalBelowThreshold() {
        // Arrange
        CreateGradeRequest request = CreateGradeRequest.builder()
                .studentId(1L)
                .subjectId(1L)
                .teacherId(1L)
                .attendanceScore(2)
                .seminarScore(2)
                .col1(2)
                .col2(2)
                .col3(2)
                .examScore(10)
                .build();

        Grade failGrade = Grade.builder()
                .id(1L)
                .student(student)
                .subject(subject)
                .teacher(teacher)
                .attendanceScore(2)
                .seminarScore(2)
                .col1(2)
                .col2(2)
                .col3(2)
                .examScore(10)
                .totalScore(20)
                .status(GradeStatus.FAILED_BY_EXAM)
                .build();

        GradeResponse failResponse = GradeResponse.builder()
                .totalScore(20)
                .status("FAIL")
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(gradeRepository.save(any(Grade.class))).thenReturn(failGrade);
        when(mapper.toGradeResponse(failGrade)).thenReturn(failResponse);

        // Act
        GradeResponse result = gradeService.createGrade(request);

        // Assert
        assertNotNull(result);
        assertEquals(20, result.totalScore());
        assertEquals("FAIL", result.status());
    }

    @Test
    void shouldThrowInvalidGradeValueExceptionForOutOfRangeAttendanceScore() {
        // Arrange
        CreateGradeRequest request = CreateGradeRequest.builder()
                .studentId(1L)
                .subjectId(1L)
                .teacherId(1L)
                .attendanceScore(11) // Invalid: should be 0-10
                .seminarScore(7)
                .col1(6)
                .col2(7)
                .col3(8)
                .examScore(35)
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        // Act & Assert
        assertThrows(InvalidGradeValueException.class, () -> gradeService.createGrade(request));
    }

    @Test
    void shouldThrowInvalidGradeValueExceptionForOutOfRangeExamScore() {
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
                .examScore(51) // Invalid: should be 0-50
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        // Act & Assert
        assertThrows(InvalidGradeValueException.class, () -> gradeService.createGrade(request));
    }

    @Test
    void shouldThrowStudentNotFoundExceptionWhenCreatingGrade() {
        // Arrange
        CreateGradeRequest request = CreateGradeRequest.builder()
                .studentId(999L)
                .subjectId(1L)
                .teacherId(1L)
                .attendanceScore(8)
                .seminarScore(7)
                .col1(6)
                .col2(7)
                .col3(8)
                .examScore(35)
                .build();

        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StudentNotFoundException.class, () -> gradeService.createGrade(request));
    }

    @Test
    void shouldThrowSubjectNotFoundExceptionWhenCreatingGrade() {
        // Arrange
        CreateGradeRequest request = CreateGradeRequest.builder()
                .studentId(1L)
                .subjectId(999L)
                .teacherId(1L)
                .attendanceScore(8)
                .seminarScore(7)
                .col1(6)
                .col2(7)
                .col3(8)
                .examScore(35)
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SubjectNotFoundException.class, () -> gradeService.createGrade(request));
    }

    @Test
    void shouldThrowTeacherNotFoundExceptionWhenCreatingGrade() {
        // Arrange
        CreateGradeRequest request = CreateGradeRequest.builder()
                .studentId(1L)
                .subjectId(1L)
                .teacherId(999L)
                .attendanceScore(8)
                .seminarScore(7)
                .col1(6)
                .col2(7)
                .col3(8)
                .examScore(35)
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TeacherNotFoundException.class, () -> gradeService.createGrade(request));
    }

    @Test
    void shouldUpdateGradeSuccessfully() {
        // Arrange
        UpdateGradeRequest request = UpdateGradeRequest.builder()
                .attendanceScore(9)
                .seminarScore(8)
                .col1(7)
                .col2(8)
                .col3(9)
                .examScore(37)
                .build();

        Grade updatedGrade = Grade.builder()
                .id(1L)
                .student(student)
                .subject(subject)
                .teacher(teacher)
                .attendanceScore(9)
                .seminarScore(8)
                .col1(7)
                .col2(8)
                .col3(9)
                .examScore(37)
                .totalScore(78)
                .status(GradeStatus.PASSED)
                .build();

        GradeResponse updatedResponse = GradeResponse.builder()
                .totalScore(78)
                .status("EXCELLENT")
                .build();

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(gradeRepository.save(any(Grade.class))).thenReturn(updatedGrade);
        when(mapper.toGradeResponse(updatedGrade)).thenReturn(updatedResponse);

        // Act
        GradeResponse result = gradeService.updateGrade(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(78, result.totalScore());
        verify(gradeRepository, times(1)).save(any(Grade.class));
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

