package com.example.dto.mapper;

import com.example.dto.response.TeacherResponse;
import com.example.entity.Teacher;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface TeacherMapper {

    TeacherResponse toTeacherResponse(Teacher teacher);
}

