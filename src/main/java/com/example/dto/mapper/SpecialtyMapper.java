package com.example.dto.mapper;

import com.example.dto.response.SpecialtyResponse;
import com.example.entity.Specialty;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SpecialtyMapper {

    SpecialtyResponse toSpecialtyResponse(Specialty specialty);
}

