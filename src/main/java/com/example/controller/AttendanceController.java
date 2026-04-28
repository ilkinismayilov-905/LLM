package com.example.controller;

import com.example.dto.request.MarkAttendanceRequest;
import com.example.dto.response.AttendanceResponse;
import com.example.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/mark")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> markAttendance(@Valid @RequestBody MarkAttendanceRequest request) {
        Object result = attendanceService.markAttendance(request);
        if (result instanceof AttendanceResponse) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<List<AttendanceResponse>> getStudentAttendance(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getStudentAttendance(studentId));
    }

    @GetMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<List<AttendanceResponse>> getLessonAttendance(@PathVariable Long lessonId) {
        return ResponseEntity.ok(attendanceService.getLessonAttendance(lessonId));
    }
}

