package com.krd.store.users;

import com.krd.starter.user.BaseUserMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends BaseUserMapper<User, UserDto> {
    // MapStruct will automatically generate implementations of inherited methods:
    // - UserDto toDto(User user)
    // - User toEntity(RegisterUserRequest request)
    // - void update(UpdateUserRequest request, @MappingTarget User user)
}
