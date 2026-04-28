package com.example.service;

import com.example.dto.mapper.AttendanceMapper;
import com.example.dto.request.MarkAttendanceRequest;
import com.example.dto.response.AttendanceResponse;
import com.example.dto.response.AttendanceWarningResponse;
import com.example.entity.*;
import com.example.enums.AttendanceStatus;
import com.example.exception.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final TeacherGroupSubjectRepository tgsRepository;
    private final AttendanceMapper mapper;

    private static final int ALLOWED_ABSENT_TO_PRESENT_MINUTES = 15;

    /**
     * Mark attendance for a student in a lesson
     * 
     * Business Rules:
     * 1. Only teacher can mark attendance
     * 2. Teacher can only mark for lessons assigned to them
     * 3. Attendance can only be changed for active lessons
     * 4. Student can be marked ABSENT anytime during active lesson
     * 5. ABSENT -> PRESENT is allowed only within first 15 minutes
     * 6. After 15 minutes, revert returns WARNING (not exception)
     * 7. If lesson is finished, no changes allowed
     * 8. If lesson not started yet, no changes allowed
     */
    public Object markAttendance(MarkAttendanceRequest request) {
        log.info("Marking attendance for student id: {} in lesson id: {}", request.studentId(), request.lessonId());

        // Validate lesson exists and is active
        Lesson lesson = lessonRepository.findByIdAndIsActiveTrue(request.lessonId())
                .orElseThrow(() -> new InvalidInputException("Lesson not found or is inactive"));

        // Validate student exists
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + request.studentId()));

        // Verify teacher authorization
        Teacher teacher = getAuthenticatedTeacher();
        validateTeacherAssignment(teacher, lesson.getTeacherGroupSubject());

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Baku"));
        log.info("Current Time: {}, Lesson Start Time: {}", now, lesson.getStartTime());
        System.out.println("Current time in Asia/Baku: " + now);

        // Check lesson timing constraints
        if (lesson.hasLessonEnded(now)) {
            throw new InvalidInputException("Cannot mark attendance: lesson has ended");
        }

        if (!lesson.hasLessonStarted(now)) {
            throw new InvalidInputException("Cannot mark attendance: lesson has not started yet");
        }

        // Handle attendance marking/updating
        Attendance attendance = attendanceRepository.findByLessonAndStudent(lesson, student)
                .orElse(null);

        if (attendance == null) {
            // New attendance record
            attendance = Attendance.builder()
                    .lesson(lesson)
                    .student(student)
                    .status(mapRequestStatusToEntity(request.status()))
                    .remarks(request.remarks())
                    .build();

            Attendance savedAttendance = attendanceRepository.save(attendance);
            log.info("Attendance marked for student id: {} as: {}", request.studentId(), request.status());
            return mapper.toAttendanceResponse(savedAttendance);
        }

        // Existing attendance record - handle update with 15-minute rule
        return handleAttendanceUpdate(attendance, request, lesson, now);
    }

    /**
     * Handle updating existing attendance with 15-minute rule for ABSENT->PRESENT
     */
    private Object handleAttendanceUpdate(Attendance attendance, MarkAttendanceRequest request, 
                                         Lesson lesson, LocalDateTime now) {
        
        AttendanceStatus newStatus = mapRequestStatusToEntity(request.status());
        AttendanceStatus currentStatus = attendance.getStatus();

        // If changing from ABSENT to PRESENT
        if (currentStatus == AttendanceStatus.ABSENT &&
            newStatus == AttendanceStatus.PRESENT) {
            
            // Check if still within first 15 minutes
            if (!lesson.isWithinFirstFifteenMinutes(now)) {
                log.warn("Cannot change ABSENT to PRESENT after 15 minutes for student id: {}", 
                        attendance.getStudent().getId());
                
                // Return warning response instead of throwing exception
                return new AttendanceWarningResponse(
                        true,
                        "Cannot change ABSENT to PRESENT after 15 minutes of lesson start",
                        currentStatus.name(),
                        newStatus.name(),
                        attendance.getId()
                );
            }
        }

        // Update attendance
        attendance.setStatus(newStatus);
        attendance.setRemarks(request.remarks());
        Attendance updatedAttendance = attendanceRepository.save(attendance);

        log.info("Attendance updated for student id: {} to: {}", 
                attendance.getStudent().getId(), newStatus);
        
        return mapper.toAttendanceResponse(updatedAttendance);
    }

    /**
     * Get attendance records for a student
     */
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getStudentAttendance(Long studentId) {
        log.info("Fetching attendance for student id: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + studentId));

        return attendanceRepository.findByStudent(student)
                .stream()
                .map(mapper::toAttendanceResponse)
                .toList();
    }

    /**
     * Get attendance records for a lesson
     */
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getLessonAttendance(Long lessonId) {
        log.info("Fetching attendance for lesson id: {}", lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new InvalidInputException("Lesson not found with id: " + lessonId));

        return attendanceRepository.findByLesson(lesson)
                .stream()
                .map(mapper::toAttendanceResponse)
                .toList();
    }

    /**
     * Get authenticated teacher
     */
    private Teacher getAuthenticatedTeacher() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedTeacherException("User not found"));

        return teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UnauthorizedTeacherException("Teacher not found for user id: " + user.getId()));
    }

    /**
     * Validate teacher is assigned to the lesson's group and subject
     */
    private void validateTeacherAssignment(Teacher teacher, TeacherGroupSubject tgs) {
        if (!tgs.getTeacher().getId().equals(teacher.getId())) {
            throw new UnauthorizedTeacherException(
                    "Teacher is not assigned to this lesson");
        }
    }

    private AttendanceStatus mapRequestStatusToEntity(String status) {
        try {
            return AttendanceStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid attendance status: " + status);
        }
    }
}

