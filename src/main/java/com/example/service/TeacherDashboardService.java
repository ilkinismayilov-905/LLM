package com.example.service;

import com.example.dto.response.GroupDto;
import com.example.dto.response.SubjectDto;
import com.example.dto.response.TeacherDashboardResponse;
import com.example.entity.Teacher;
import com.example.entity.TeacherGroupSubject;
import com.example.enums.Role;
import com.example.exception.TeacherNotFoundException;
import com.example.exception.UnauthorizedTeacherException;
import com.example.repository.TeacherGroupSubjectRepository;
import com.example.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TeacherDashboardService {

    private final TeacherRepository teacherRepository;
    private final TeacherGroupSubjectRepository teacherGroupSubjectRepository;

    /**
     * Get teacher dashboard data including assigned groups and subjects
     *
     * @param userId The authenticated user's ID
     * @return TeacherDashboardResponse containing profile and assignments
     */
    public TeacherDashboardResponse getTeacherDashboardData(Long userId) {
        log.info("Fetching dashboard data for user ID: {}", userId);

        Teacher teacher = teacherRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> {
                    log.warn("Teacher not found for user ID: {}", userId);
                    return new TeacherNotFoundException("Teacher profile not found");
                });

        if (teacher.getUser().getRole() != Role.TEACHER) {
            log.warn("User ID: {} is not a teacher", userId);
            throw new UnauthorizedTeacherException("User is not authorized as a teacher");
        }

        List<TeacherGroupSubject> assignments = teacherGroupSubjectRepository.findAllByTeacherId(teacher.getId());

        List<GroupDto> assignedGroups = assignments.stream()
                .map(TeacherGroupSubject::getGroup)
                .distinct()
                .map(group -> GroupDto.builder()
                        .id(group.getId())
                        .groupNumber(group.getGroupNumber())
                        .build())
                .collect(Collectors.toList());

        List<SubjectDto> assignedSubjects = assignments.stream()
                .map(TeacherGroupSubject::getSubject)
                .distinct()
                .map(subject -> SubjectDto.builder()
                        .id(subject.getId())
                        .name(subject.getName())
                        .credits(subject.getCredits())
                        .build())
                .collect(Collectors.toList());

        return TeacherDashboardResponse.builder()
                .firstName(teacher.getUser().getFirstName())
                .lastName(teacher.getUser().getLastName())
                .email(teacher.getUser().getEmail())
                .department(teacher.getDepartment())
                .assignedGroups(assignedGroups)
                .assignedSubjects(assignedSubjects)
                .build();
    }
}
