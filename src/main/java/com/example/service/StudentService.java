package com.example.service;

import com.example.dto.mapper.StudentMapper;
import com.example.dto.request.CreateStudentRequest;
import com.example.dto.request.UpdateStudentRequest;
import com.example.dto.response.StudentResponse;
import com.example.entity.Group;
import com.example.entity.Student;
import com.example.entity.User;
import com.example.exception.DuplicateStudentException;
import com.example.exception.GroupNotFoundException;
import com.example.exception.StudentNotFoundException;
import com.example.exception.UserNotFoundException;
import com.example.repository.GroupRepository;
import com.example.repository.StudentRepository;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final StudentMapper mapper;

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        log.info("Fetching student with id: {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id));
        return mapper.toStudentResponse(student);
    }
    @Transactional(readOnly = true)
    public StudentResponse getStudentByUserId(Long id) {
        log.info("Fetching student with user id: {}", id);
        Student student = studentRepository.findByUserId(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with user id: " + id));
        return mapper.toStudentResponse(student);
    }


    @Transactional(readOnly = true)
    public StudentResponse getStudentByStudentNumber(String studentNumber) {
        log.info("Fetching student with number: {}", studentNumber);
        Student student = studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with number: " + studentNumber));
        return mapper.toStudentResponse(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        log.info("Fetching all students");
        return studentRepository.findAll()
                .stream()
                .map(mapper::toStudentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByGroupId(Long groupId) {
        log.info("Fetching students for group id: {}", groupId);
        return studentRepository.findAllByGroupId(groupId)
                .stream()
                .map(mapper::toStudentResponse)
                .toList();
    }

    public StudentResponse createStudent(CreateStudentRequest request) {
        log.info("Creating new student");

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.userId()));

        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + request.groupId()));

        if (studentRepository.existsByStudentNumber(request.studentNumber())) {
            throw new DuplicateStudentException("Student already exists with number: " + request.studentNumber());
        }

        Student student = Student.builder()
                .user(user)
                .studentNumber(request.studentNumber())
                .group(group)
                .build();

        Student savedStudent = studentRepository.save(student);
        log.info("Student created successfully with id: {}", savedStudent.getId());

        return mapper.toStudentResponse(savedStudent);
    }

    public StudentResponse updateStudent(Long id, UpdateStudentRequest request) {
        log.info("Updating student with id: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id));

        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + request.groupId()));

        student.setGroup(group);

        Student updatedStudent = studentRepository.save(student);
        log.info("Student updated successfully with id: {}", updatedStudent.getId());

        return mapper.toStudentResponse(updatedStudent);
    }

    public void deleteStudent(Long id) {
        log.info("Deleting student with id: {}", id);

        if (!studentRepository.existsById(id)) {
            throw new StudentNotFoundException("Student not found with id: " + id);
        }

        studentRepository.deleteById(id);
        log.info("Student deleted successfully with id: {}", id);
    }
}

