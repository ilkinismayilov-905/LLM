package com.example.dto.mapper;

import com.example.dto.response.GroupResponse;
import com.example.entity.Group;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = SpecialtyMapper.class)
public interface GroupMapper {

    GroupResponse toGroupResponse(Group group);
}

