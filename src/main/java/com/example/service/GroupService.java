package com.example.service;

import com.example.dto.mapper.GroupMapper;
import com.example.dto.request.CreateGroupRequest;
import com.example.dto.request.UpdateGroupRequest;
import com.example.dto.response.GroupResponse;
import com.example.entity.Group;
import com.example.entity.Specialty;
import com.example.exception.GroupNotFoundException;
import com.example.exception.InvalidInputException;
import com.example.exception.SpecialtyNotFoundException;
import com.example.repository.GroupRepository;
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
public class GroupService {

    private final GroupRepository groupRepository;
    private final SpecialtyRepository specialtyRepository;
    private final GroupMapper mapper;

    @Transactional(readOnly = true)
    public GroupResponse getGroupById(Long id) {
        log.info("Fetching group with id: {}", id);
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + id));
        return mapper.toGroupResponse(group);
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroupByGroupNumber(String groupNumber) {
        log.info("Fetching group with number: {}", groupNumber);
        Group group = groupRepository.findByGroupNumber(groupNumber)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with number: " + groupNumber));
        return mapper.toGroupResponse(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getAllGroups() {
        log.info("Fetching all groups");
        return groupRepository.findAll()
                .stream()
                .map(mapper::toGroupResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getGroupsBySpecialtyId(Long specialtyId) {
        log.info("Fetching groups for specialty id: {}", specialtyId);
        return groupRepository.findAllBySpecialtyId(specialtyId)
                .stream()
                .map(mapper::toGroupResponse)
                .toList();
    }

    public GroupResponse createGroup(CreateGroupRequest request) {
        log.info("Creating new group");
        if (groupRepository.existsByGroupNumber(request.groupNumber())) {
            throw new InvalidInputException("Group with number " + request.groupNumber() + " already exists");
        }

        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new SpecialtyNotFoundException("Specialty not found with id: " + request.specialtyId()));

        Group group = Group.builder()
                .groupNumber(request.groupNumber())
                .specialty(specialty)
                .build();

        Group savedGroup = groupRepository.save(group);
        log.info("Group created successfully with id: {}", savedGroup.getId());

        return mapper.toGroupResponse(savedGroup);
    }

    public GroupResponse updateGroup(Long id, UpdateGroupRequest request) {
        log.info("Updating group with id: {}", id);

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + id));

        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new SpecialtyNotFoundException("Specialty not found with id: " + request.specialtyId()));

        group.setSpecialty(specialty);

        Group updatedGroup = groupRepository.save(group);
        log.info("Group updated successfully with id: {}", updatedGroup.getId());

        return mapper.toGroupResponse(updatedGroup);
    }

    public void deleteGroup(Long id) {
        log.info("Deleting group with id: {}", id);

        if (!groupRepository.existsById(id)) {
            throw new GroupNotFoundException("Group not found with id: " + id);
        }

        groupRepository.deleteById(id);
        log.info("Group deleted successfully with id: {}", id);
    }
}

