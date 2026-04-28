package com.example.controller;

import com.example.dto.request.CreateStudentRequest;
import com.example.dto.request.UpdateStudentRequest;
import com.example.dto.response.StudentResponse;
import com.example.security.UserDetailsImpl;
import com.example.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/student-number/{studentNumber}")
    public ResponseEntity<StudentResponse> getStudentByStudentNumber(@PathVariable String studentNumber) {
        return ResponseEntity.ok(studentService.getStudentByStudentNumber(studentNumber));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<StudentResponse>> getStudentsByGroupId(@PathVariable Long groupId) {
        return ResponseEntity.ok(studentService.getStudentsByGroupId(groupId));
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<StudentResponse> getStudentMe(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(studentService.getStudentById(userDetails.getId()));
    }
}

