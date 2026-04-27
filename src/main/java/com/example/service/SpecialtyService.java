package com.example.service;

import com.example.dto.mapper.SpecialtyMapper;
import com.example.dto.request.CreateSpecialtyRequest;
import com.example.dto.request.UpdateSpecialtyRequest;
import com.example.dto.response.SpecialtyResponse;
import com.example.entity.Specialty;
import com.example.exception.DuplicateUserException;
import com.example.exception.SpecialtyNotFoundException;
import com.example.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper mapper;

    @Transactional(readOnly = true)
    public SpecialtyResponse getSpecialtyById(Long id) {
        log.info("Fetching specialty with id: {}", id);
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new SpecialtyNotFoundException("Specialty not found with id: " + id));
        return mapper.toSpecialtyResponse(specialty);
    }

    @Transactional(readOnly = true)
    public SpecialtyResponse getSpecialtyByName(String name) {
        log.info("Fetching specialty with name: {}", name);
        Specialty specialty = specialtyRepository.findByName(name)
                .orElseThrow(() -> new SpecialtyNotFoundException("Specialty not found with name: " + name));
        return mapper.toSpecialtyResponse(specialty);
    }

    @Transactional(readOnly = true)
    public List<SpecialtyResponse> getAllSpecialties() {
        log.info("Fetching all specialties");
        return specialtyRepository.findAll()
                .stream()
                .map(mapper::toSpecialtyResponse)
                .toList();
    }

    public SpecialtyResponse createSpecialty(CreateSpecialtyRequest request) {
        log.info("Creating new specialty");

        if (specialtyRepository.existsByName(request.name())) {
            throw new DuplicateUserException("Specialty already exists with name: " + request.name());
        }

        Specialty specialty = Specialty.builder()
                .name(request.name())
                .build();

        Specialty savedSpecialty = specialtyRepository.save(specialty);
        log.info("Specialty created successfully with id: {}", savedSpecialty.getId());

        return mapper.toSpecialtyResponse(savedSpecialty);
    }

    public SpecialtyResponse updateSpecialty(Long id, UpdateSpecialtyRequest request) {
        log.info("Updating specialty with id: {}", id);

        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new SpecialtyNotFoundException("Specialty not found with id: " + id));

        if (!specialty.getName().equals(request.name()) && specialtyRepository.existsByName(request.name())) {
            throw new DuplicateUserException("Specialty already exists with name: " + request.name());
        }

        specialty.setName(request.name());

        Specialty updatedSpecialty = specialtyRepository.save(specialty);
        log.info("Specialty updated successfully with id: {}", updatedSpecialty.getId());

        return mapper.toSpecialtyResponse(updatedSpecialty);
    }

    public void deleteSpecialty(Long id) {
        log.info("Deleting specialty with id: {}", id);

        if (!specialtyRepository.existsById(id)) {
            throw new SpecialtyNotFoundException("Specialty not found with id: " + id);
        }

        specialtyRepository.deleteById(id);
        log.info("Specialty deleted successfully with id: {}", id);
    }
}

