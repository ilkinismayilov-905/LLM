package com.example.dto.mapper;

import com.example.dto.response.SubjectResponse;
import com.example.entity.Subject;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    SubjectResponse toSubjectResponse(Subject subject);
}

