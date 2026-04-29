package com.example.dto.response;

import lombok.Builder;

@Builder
public record SubjectDto(
    Long id,
    String name,
    Integer credits
) {
}
