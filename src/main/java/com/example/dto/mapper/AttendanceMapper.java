package com.example.dto.mapper;

import com.example.dto.response.AttendanceResponse;
import com.example.entity.Attendance;
import com.example.enums.AttendanceStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = StudentMapper.class)
public interface AttendanceMapper {

    @Mapping(source = "lesson.id", target = "lessonId")
    @Mapping(source = "status", target = "status", qualifiedByName = "attendanceStatusToString")
    AttendanceResponse toAttendanceResponse(Attendance attendance);

    @Named("attendanceStatusToString")
    default String attendanceStatusToString(AttendanceStatus status) {
        return status != null ? status.name() : null;
    }
}


