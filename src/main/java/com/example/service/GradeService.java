package com.example.service;

import com.example.dto.mapper.GradeMapper;
import com.example.dto.request.CreateGradeRequest;
import com.example.dto.request.UpdateGradeRequest;
import com.example.dto.response.GradeResponse;
import com.example.entity.Grade;
import com.example.entity.Student;
import com.example.entity.Subject;
import com.example.entity.Teacher;
import com.example.enums.GradeStatus;
import com.example.exception.GradeNotFoundException;
import com.example.exception.InvalidGradeValueException;
import com.example.exception.StudentNotFoundException;
import com.example.exception.SubjectNotFoundException;
import com.example.exception.TeacherNotFoundException;
import com.example.repository.GradeRepository;
import com.example.repository.StudentRepository;
import com.example.repository.SubjectRepository;
import com.example.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final GradeMapper mapper;

    @Transactional(readOnly = true)
    public GradeResponse getGradeById(Long id) {
        log.info("Fetching grade with id: {}", id);
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new GradeNotFoundException("Grade not found with id: " + id));
        return mapper.toGradeResponse(grade);
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesByStudentId(Long studentId) {
        log.info("Fetching grades for student id: {}", studentId);
        return gradeRepository.findAllByStudentId(studentId)
                .stream()
                .map(mapper::toGradeResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesByTeacherId(Long teacherId) {
        log.info("Fetching grades for teacher id: {}", teacherId);
        return gradeRepository.findAllByTeacherId(teacherId)
                .stream()
                .map(mapper::toGradeResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getAllGrades() {
        log.info("Fetching all grades");
        return gradeRepository.findAll()
                .stream()
                .map(mapper::toGradeResponse)
                .toList();
    }

    public GradeResponse createGrade(CreateGradeRequest request) {
        log.info("Creating new grade");

        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + request.studentId()));

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found with id: " + request.subjectId()));

        Teacher teacher = teacherRepository.findById(request.teacherId())
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id: " + request.teacherId()));

        validateGradeValues(request);

        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setSubject(subject);
        grade.setTeacher(teacher);
        grade.setAttendanceScore(request.attendanceScore());
        grade.setSeminarScore(request.seminarScore());
        grade.setCol1(request.col1());
        grade.setCol2(request.col2());
        grade.setCol3(request.col3());
        grade.setExamScore(request.examScore());

        int totalScore = calculateTotalScore(request);
        int examScore = request.seminarScore();
        grade.setTotalScore(totalScore);
        grade.setStatus(getGradeStatus(totalScore,examScore));

        Grade savedGrade = gradeRepository.save(grade);
        log.info("Grade created successfully with id: {}", savedGrade.getId());

        return mapper.toGradeResponse(savedGrade);
    }

    public GradeResponse updateGrade(Long id, UpdateGradeRequest request) {
        log.info("Updating grade with id: {}", id);

        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new GradeNotFoundException("Grade not found with id: " + id));

        validateGradeValues(request);

        if (request.attendanceScore() != null) {
            grade.setAttendanceScore(request.attendanceScore());
        }
        if (request.seminarScore() != null) {
            grade.setSeminarScore(request.seminarScore());
        }
        if (request.col1() != null) {
            grade.setCol1(request.col1());
        }
        if (request.col2() != null) {
            grade.setCol2(request.col2());
        }
        if (request.col3() != null) {
            grade.setCol3(request.col3());
        }
        if (request.examScore() != null) {
            grade.setExamScore(request.examScore());
        }

        int totalScore = calculateTotalScore(grade);
        int examScore = request.examScore();
        grade.setTotalScore(totalScore);
        grade.setStatus(getGradeStatus(totalScore,examScore));

        Grade updatedGrade = gradeRepository.save(grade);
        log.info("Grade updated successfully with id: {}", updatedGrade.getId());

        return mapper.toGradeResponse(updatedGrade);
    }

    public void deleteGrade(Long id) {
        log.info("Deleting grade with id: {}", id);

        if (!gradeRepository.existsById(id)) {
            throw new GradeNotFoundException("Grade not found with id: " + id);
        }

        gradeRepository.deleteById(id);
        log.info("Grade deleted successfully with id: {}", id);
    }

    private void validateGradeValues(CreateGradeRequest request) {
        if ((request.attendanceScore() != null && (request.attendanceScore() < 0 || request.attendanceScore() > 10)) ||
                (request.seminarScore() != null && (request.seminarScore() < 0 || request.seminarScore() > 10)) ||
                (request.col1() != null && (request.col1() < 0 || request.col1() > 10)) ||
                (request.col2() != null && (request.col2() < 0 || request.col2() > 10)) ||
                (request.col3() != null && (request.col3() < 0 || request.col3() > 10)) ||
                (request.examScore() != null && (request.examScore() < 0 || request.examScore() > 50))) {
            throw new InvalidGradeValueException("Invalid grade value. Scores must be within allowed ranges.");
        }
    }

    private void validateGradeValues(UpdateGradeRequest request) {
        if ((request.attendanceScore() != null && (request.attendanceScore() < 0 || request.attendanceScore() > 10)) ||
                (request.seminarScore() != null && (request.seminarScore() < 0 || request.seminarScore() > 10)) ||
                (request.col1() != null && (request.col1() < 0 || request.col1() > 10)) ||
                (request.col2() != null && (request.col2() < 0 || request.col2() > 10)) ||
                (request.col3() != null && (request.col3() < 0 || request.col3() > 10)) ||
                (request.examScore() != null && (request.examScore() < 0 || request.examScore() > 50))) {
            throw new InvalidGradeValueException("Invalid grade value. Scores must be within allowed ranges.");
        }
    }

    private int calculateTotalScore(CreateGradeRequest request) {
        int total = 0;
        if (request.attendanceScore() != null) total += request.attendanceScore();
        if (request.seminarScore() != null) total += request.seminarScore();
        if (request.col1() != null) total += request.col1();
        if (request.col2() != null) total += request.col2();
        if (request.col3() != null) total += request.col3();
        if (request.examScore() != null) total += request.examScore();
        return total;
    }

    private int calculateTotalScore(Grade grade) {
        int total = 0;
        if (grade.getAttendanceScore() != null) total += grade.getAttendanceScore();
        if (grade.getSeminarScore() != null) total += grade.getSeminarScore();
        if (grade.getCol1() != null) total += grade.getCol1();
        if (grade.getCol2() != null) total += grade.getCol2();
        if (grade.getCol3() != null) total += grade.getCol3();
        if (grade.getExamScore() != null) total += grade.getExamScore();
        return total;
    }

    private GradeStatus getGradeStatus(int totalScore,int examScore) {
        if (totalScore >= 51 && examScore >= 17) return GradeStatus.PASSED;
        if (totalScore <= 50) return GradeStatus.FAILED_BY_TOTAL;
        if (examScore <= 17) return GradeStatus.FAILED_BY_EXAM;
        return GradeStatus.PENDING;
    }
}
