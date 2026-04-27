package com.example.dto.mapper;

import com.example.dto.response.GradeResponse;
import com.example.entity.Grade;
import com.example.enums.GradeStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {StudentMapper.class, SubjectMapper.class, TeacherMapper.class})
public interface GradeMapper {

    @Mapping(source = "status", target = "status", qualifiedByName = "gradeStatusToString")
    GradeResponse toGradeResponse(Grade grade);

    @Named("gradeStatusToString")
    default String gradeStatusToString(GradeStatus status) {
        return status != null ? status.name() : null;
    }
}


