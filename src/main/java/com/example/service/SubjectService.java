package com.example.service;

import com.example.dto.mapper.SubjectMapper;
import com.example.dto.request.CreateSubjectRequest;
import com.example.dto.request.UpdateSubjectRequest;
import com.example.dto.response.SubjectResponse;
import com.example.entity.Subject;
import com.example.exception.DuplicateUserException;
import com.example.exception.SubjectNotFoundException;
import com.example.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper mapper;

    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Long id) {
        log.info("Fetching subject with id: {}", id);
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found with id: " + id));
        return mapper.toSubjectResponse(subject);
    }

    @Transactional(readOnly = true)
    public SubjectResponse getSubjectByName(String name) {
        log.info("Fetching subject with name: {}", name);
        Subject subject = subjectRepository.findByName(name);
        if (subject == null) {
            throw new SubjectNotFoundException("Subject not found with name: " + name);
        }
        return mapper.toSubjectResponse(subject);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        log.info("Fetching all subjects");
        return subjectRepository.findAll()
                .stream()
                .map(mapper::toSubjectResponse)
                .toList();
    }

    public SubjectResponse createSubject(CreateSubjectRequest request) {
        log.info("Creating new subject");

        if (subjectRepository.existsByName(request.name())) {
            throw new DuplicateUserException("Subject already exists with name: " + request.name());
        }

        Subject subject = Subject.builder()
                .name(request.name())
                .credits(request.credits())
                .build();

        Subject savedSubject = subjectRepository.save(subject);
        log.info("Subject created successfully with id: {}", savedSubject.getId());

        return mapper.toSubjectResponse(savedSubject);
    }

    public SubjectResponse updateSubject(Long id, UpdateSubjectRequest request) {
        log.info("Updating subject with id: {}", id);

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found with id: " + id));

        subject.setCredits(request.credits());

        Subject updatedSubject = subjectRepository.save(subject);
        log.info("Subject updated successfully with id: {}", updatedSubject.getId());

        return mapper.toSubjectResponse(updatedSubject);
    }

    public void deleteSubject(Long id) {
        log.info("Deleting subject with id: {}", id);

        if (!subjectRepository.existsById(id)) {
            throw new SubjectNotFoundException("Subject not found with id: " + id);
        }

        subjectRepository.deleteById(id);
        log.info("Subject deleted successfully with id: {}", id);
    }
}

