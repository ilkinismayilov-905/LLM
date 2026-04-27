package com.example.service;

import com.example.dto.mapper.TeacherMapper;
import com.example.dto.request.CreateTeacherRequest;
import com.example.dto.request.UpdateTeacherRequest;
import com.example.dto.response.TeacherResponse;
import com.example.entity.Teacher;
import com.example.entity.User;
import com.example.exception.TeacherNotFoundException;
import com.example.exception.UserNotFoundException;
import com.example.repository.TeacherRepository;
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
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final TeacherMapper mapper;

    @Transactional(readOnly = true)
    public TeacherResponse getTeacherById(Long id) {
        log.info("Fetching teacher with id: {}", id);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id: " + id));
        return mapper.toTeacherResponse(teacher);
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacherByUserId(Long userId) {
        log.info("Fetching teacher for user id: {}", userId);
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found for user id: " + userId));
        return mapper.toTeacherResponse(teacher);
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAllTeachers() {
        log.info("Fetching all teachers");
        return teacherRepository.findAll()
                .stream()
                .map(mapper::toTeacherResponse)
                .toList();
    }

    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        log.info("Creating new teacher");

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.userId()));

        Teacher teacher = Teacher.builder()
                .user(user)
                .department(request.department())
                .build();

        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("Teacher created successfully with id: {}", savedTeacher.getId());

        return mapper.toTeacherResponse(savedTeacher);
    }

    public TeacherResponse updateTeacher(Long id, UpdateTeacherRequest request) {
        log.info("Updating teacher with id: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id: " + id));

        teacher.setDepartment(request.department());

        Teacher updatedTeacher = teacherRepository.save(teacher);
        log.info("Teacher updated successfully with id: {}", updatedTeacher.getId());

        return mapper.toTeacherResponse(updatedTeacher);
    }

    public void deleteTeacher(Long id) {
        log.info("Deleting teacher with id: {}", id);

        if (!teacherRepository.existsById(id)) {
            throw new TeacherNotFoundException("Teacher not found with id: " + id);
        }

        teacherRepository.deleteById(id);
        log.info("Teacher deleted successfully with id: {}", id);
    }
}

