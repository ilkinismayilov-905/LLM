package com.example.dto.response;

import lombok.Builder;

@Builder
public record GroupDto(
    Long id,
    String groupNumber
) {
}
