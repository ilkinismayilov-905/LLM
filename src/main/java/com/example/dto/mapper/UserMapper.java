package com.example.dto.mapper;

import com.example.dto.response.UserResponse;
import com.example.entity.User;
import com.example.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role", target = "role", qualifiedByName = "roleToString")
    UserResponse toUserResponse(User user);

    @Named("roleToString")
    default String roleToString(Role role) {
        return role != null ? role.name() : null;
    }
}


