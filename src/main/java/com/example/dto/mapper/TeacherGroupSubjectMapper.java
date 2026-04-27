package com.example.dto.mapper;

import com.example.dto.response.TeacherGroupSubjectResponse;
import com.example.entity.TeacherGroupSubject;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {TeacherMapper.class, GroupMapper.class, SubjectMapper.class})
public interface TeacherGroupSubjectMapper {

    TeacherGroupSubjectResponse toTeacherGroupSubjectResponse(TeacherGroupSubject tgs);
}

