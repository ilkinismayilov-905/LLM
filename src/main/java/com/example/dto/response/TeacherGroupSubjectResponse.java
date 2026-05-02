package com.example.dto.response;

import lombok.Builder;

@Builder
public record TeacherGroupSubjectResponse(
    Long id,
    TeacherResponse teacher,
    GroupResponse group,
    SubjectResponse subject
) {
}

