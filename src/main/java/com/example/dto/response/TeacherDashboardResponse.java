package com.example.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record TeacherDashboardResponse(
    String firstName,
    String lastName,
    String email,
    String department,
    List<GroupDto> assignedGroups,
    List<SubjectDto> assignedSubjects
) {
}
