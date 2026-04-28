package com.example.controller;

import com.example.dto.request.CreateGradeRequest;
import com.example.dto.request.UpdateGradeRequest;
import com.example.dto.response.GradeResponse;
import com.example.security.UserDetailsImpl;
import com.example.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping
    public ResponseEntity<List<GradeResponse>> getAllGrades() {
        return ResponseEntity.ok(gradeService.getAllGrades());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GradeResponse> getGradeById(@PathVariable Long id) {
        return ResponseEntity.ok(gradeService.getGradeById(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<GradeResponse>> getGradesByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(gradeService.getGradesByStudentId(studentId));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<GradeResponse>> getGradesByTeacherId(@PathVariable Long teacherId) {
        return ResponseEntity.ok(gradeService.getGradesByTeacherId(teacherId));
    }

    @PostMapping
    public ResponseEntity<GradeResponse> createGrade(@Valid @RequestBody CreateGradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.createGrade(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradeResponse> updateGrade(@PathVariable Long id, @Valid @RequestBody UpdateGradeRequest request) {
        return ResponseEntity.ok(gradeService.updateGrade(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student/me")
    public ResponseEntity<List<GradeResponse>> getGradesByStudentMe(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(gradeService.getGradesByStudentId(userDetails.getId()));
    }
}

