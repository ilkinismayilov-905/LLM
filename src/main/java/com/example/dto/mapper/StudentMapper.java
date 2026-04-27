package com.example.dto.mapper;

import com.example.dto.response.StudentResponse;
import com.example.entity.Student;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, GroupMapper.class})
public interface StudentMapper {

    StudentResponse toStudentResponse(Student student);
}

