package com.example.service;

import com.example.dto.mapper.TeacherGroupSubjectMapper;
import com.example.dto.request.CreateTeacherGroupSubjectRequest;
import com.example.dto.response.TeacherGroupSubjectResponse;
import com.example.entity.Group;
import com.example.entity.Subject;
import com.example.entity.Teacher;
import com.example.entity.TeacherGroupSubject;
import com.example.exception.GroupNotFoundException;
import com.example.exception.InvalidInputException;
import com.example.exception.SubjectNotFoundException;
import com.example.exception.TeacherNotFoundException;
import com.example.repository.GroupRepository;
import com.example.repository.SubjectRepository;
import com.example.repository.TeacherGroupSubjectRepository;
import com.example.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherGroupSubjectService {

    private final TeacherGroupSubjectRepository tgsRepository;
    private final TeacherRepository teacherRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherGroupSubjectMapper mapper;

    @Transactional(readOnly = true)
    public TeacherGroupSubjectResponse getTeacherGroupSubjectById(Long id) {
        log.info("Fetching teacher group subject with id: {}", id);
        TeacherGroupSubject tgs = tgsRepository.findById(id)
                .orElseThrow(() -> new InvalidInputException("Teacher group subject not found with id: " + id));
        return mapper.toTeacherGroupSubjectResponse(tgs);
    }

    @Transactional(readOnly = true)
    public List<TeacherGroupSubjectResponse> getAllTeacherGroupSubjects() {
        log.info("Fetching all teacher group subjects");
        return tgsRepository.findAll()
                .stream()
                .map(mapper::toTeacherGroupSubjectResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeacherGroupSubjectResponse> getTeacherGroupSubjectsByTeacherId(Long teacherId) {
        log.info("Fetching teacher group subjects for teacher id: {}", teacherId);
        return tgsRepository.findAllByTeacherId(teacherId)
                .stream()
                .map(mapper::toTeacherGroupSubjectResponse)
                .toList();
    }

    public TeacherGroupSubjectResponse createTeacherGroupSubject(CreateTeacherGroupSubjectRequest request) {
        log.info("Creating new teacher group subject assignment");

        Teacher teacher = teacherRepository.findById(request.teacherId())
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id: " + request.teacherId()));

        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + request.groupId()));

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found with id: " + request.subjectId()));

        if (tgsRepository.existsByTeacherIdAndGroupIdAndSubjectId(request.teacherId(), request.groupId(), request.subjectId())) {
            throw new InvalidInputException("Teacher group subject assignment already exists");
        }

        TeacherGroupSubject tgs = TeacherGroupSubject.builder()
                .teacher(teacher)
                .group(group)
                .subject(subject)
                .build();

        TeacherGroupSubject savedTgs = tgsRepository.save(tgs);
        log.info("Teacher group subject assignment created successfully with id: {}", savedTgs.getId());

        return mapper.toTeacherGroupSubjectResponse(savedTgs);
    }

    public void deleteTeacherGroupSubject(Long id) {
        log.info("Deleting teacher group subject with id: {}", id);

        if (!tgsRepository.existsById(id)) {
            throw new InvalidInputException("Teacher group subject not found with id: " + id);
        }

        tgsRepository.deleteById(id);
        log.info("Teacher group subject deleted successfully with id: {}", id);
    }
}

