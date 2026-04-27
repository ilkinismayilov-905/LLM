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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private GroupMapper mapper;

    @InjectMocks
    private GroupService groupService;

    private Group group;
    private Specialty specialty;
    private GroupResponse groupResponse;

    @BeforeEach
    void setUp() {
        specialty = Specialty.builder()
                .id(1L)
                .name("Computer Science")
                .build();

        group = Group.builder()
                .id(1L)
                .groupNumber("CS-101")
                .specialty(specialty)
                .build();

        groupResponse = GroupResponse.builder()
                .id(1L)
                .groupNumber("CS-101")
                .build();
    }

    @Test
    void shouldGetAllGroupsSuccessfully() {
        // Arrange
        List<Group> groups = Arrays.asList(group);
        List<GroupResponse> responses = Arrays.asList(groupResponse);
        when(groupRepository.findAll()).thenReturn(groups);
        when(mapper.toGroupResponse(group)).thenReturn(groupResponse);

        // Act
        List<GroupResponse> result = groupService.getAllGroups();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(groupResponse.groupNumber(), result.get(0).groupNumber());
        verify(groupRepository, times(1)).findAll();
    }

    @Test
    void shouldGetGroupByIdSuccessfully() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(mapper.toGroupResponse(group)).thenReturn(groupResponse);

        // Act
        GroupResponse result = groupService.getGroupById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(groupResponse.groupNumber(), result.groupNumber());
        verify(groupRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowGroupNotFoundExceptionWhenGroupDoesNotExist() {
        // Arrange
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(GroupNotFoundException.class, () -> groupService.getGroupById(999L));
    }

    @Test
    void shouldGetGroupByGroupNumberSuccessfully() {
        // Arrange
        when(groupRepository.findByGroupNumber("CS-101")).thenReturn(Optional.of(group));
        when(mapper.toGroupResponse(group)).thenReturn(groupResponse);

        // Act
        GroupResponse result = groupService.getGroupByGroupNumber("CS-101");

        // Assert
        assertNotNull(result);
        assertEquals(groupResponse.groupNumber(), result.groupNumber());
        verify(groupRepository, times(1)).findByGroupNumber("CS-101");
    }

    @Test
    void shouldCreateGroupSuccessfully() {
        // Arrange
        CreateGroupRequest request = CreateGroupRequest.builder()
                .groupNumber("CS-101")
                .specialtyId(1L)
                .build();

        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(specialty));
        when(groupRepository.existsByGroupNumber("CS-101")).thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenReturn(group);
        when(mapper.toGroupResponse(group)).thenReturn(groupResponse);

        // Act
        GroupResponse result = groupService.createGroup(request);

        // Assert
        assertNotNull(result);
        assertEquals(groupResponse.groupNumber(), result.groupNumber());
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void shouldThrowInvalidInputExceptionWhenGroupNumberAlreadyExists() {
        // Arrange
        CreateGroupRequest request = CreateGroupRequest.builder()
                .groupNumber("CS-101")
                .specialtyId(1L)
                .build();

        when(groupRepository.existsByGroupNumber("CS-101")).thenReturn(true);

//        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(specialty));

        // Act & Assert
        assertThrows(InvalidInputException.class, () -> groupService.createGroup(request));
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldThrowSpecialtyNotFoundExceptionWhenCreatingGroup() {
        // Arrange
        CreateGroupRequest request = CreateGroupRequest.builder()
                .groupNumber("CS-101")
                .specialtyId(999L)
                .build();

        when(specialtyRepository.findById(999L)).thenReturn(Optional.empty());
//        when(groupRepository.existsByGroupNumber("CS-101")).thenReturn(false);


        // Act & Assert
        assertThrows(SpecialtyNotFoundException.class, () -> groupService.createGroup(request));
    }

    @Test
    void shouldUpdateGroupSuccessfully() {
        // Arrange
        UpdateGroupRequest updateRequest = UpdateGroupRequest.builder()
                .specialtyId(2L)
                .build();

        Specialty newSpecialty = Specialty.builder()
                .id(2L)
                .name("Engineering")
                .build();

        Group updatedGroup = Group.builder()
                .id(1L)
                .groupNumber("CS-101")
                .specialty(newSpecialty)
                .build();

        GroupResponse updatedResponse = GroupResponse.builder()
                .id(1L)
                .groupNumber("CS-101")
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(specialtyRepository.findById(2L)).thenReturn(Optional.of(newSpecialty));
        when(groupRepository.save(any(Group.class))).thenReturn(updatedGroup);
        when(mapper.toGroupResponse(updatedGroup)).thenReturn(updatedResponse);

        // Act
        GroupResponse result = groupService.updateGroup(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void shouldThrowGroupNotFoundExceptionWhenUpdatingNonExistentGroup() {
        // Arrange
        UpdateGroupRequest updateRequest = UpdateGroupRequest.builder()
                .specialtyId(2L)
                .build();

        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(GroupNotFoundException.class, () -> groupService.updateGroup(999L, updateRequest));
    }

    @Test
    void shouldDeleteGroupSuccessfully() {
        // Arrange
        when(groupRepository.existsById(1L)).thenReturn(true);

        // Act
        groupService.deleteGroup(1L);

        // Assert
        verify(groupRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowGroupNotFoundExceptionWhenDeletingNonExistentGroup() {
        // Arrange
        when(groupRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(GroupNotFoundException.class, () -> groupService.deleteGroup(999L));
    }

    @Test
    void shouldGetGroupsBySpecialtyIdSuccessfully() {
        // Arrange
        List<Group> groups = Arrays.asList(group);
        List<GroupResponse> responses = Arrays.asList(groupResponse);
        when(groupRepository.findAllBySpecialtyId(1L)).thenReturn(groups);
        when(mapper.toGroupResponse(group)).thenReturn(groupResponse);

        // Act
        List<GroupResponse> result = groupService.getGroupsBySpecialtyId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(groupRepository, times(1)).findAllBySpecialtyId(1L);
    }
}

