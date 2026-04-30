package com.example.controller;

import com.example.dto.response.TeacherDashboardResponse;
import com.example.security.UserDetailsImpl;
import com.example.service.TeacherDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teacher/dashboard")
@RequiredArgsConstructor
@Slf4j
public class TeacherDashboardController {

    private final TeacherDashboardService teacherDashboardService;

    /**
     * Get the authenticated teacher's dashboard data
     *
     * Endpoint: GET /api/v1/teacher/dashboard
     * Security: TEACHER role required
     * Returns: Teacher profile, assigned groups and subjects
     *
     * @param authentication Spring Security authentication containing the authenticated user
     * @return TeacherDashboardResponse with dashboard data
     */
    @GetMapping
    public ResponseEntity<TeacherDashboardResponse> getTeacherDashboard(Authentication authentication) {
        log.info("GET /api/v1/teacher/dashboard endpoint called");

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        TeacherDashboardResponse dashboardData = teacherDashboardService.getTeacherDashboardData(userId);
        log.info("Successfully retrieved dashboard data for teacher user ID: {}", userId);

        return ResponseEntity.ok(dashboardData);
    }
}
