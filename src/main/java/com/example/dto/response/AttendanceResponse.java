package com.example.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AttendanceResponse(
    Long id,
    Long lessonId,
    StudentResponse student,
    String status,
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    LocalDateTime markedAt,
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    LocalDateTime lastModifiedAt,
    String remarks
) {
}

